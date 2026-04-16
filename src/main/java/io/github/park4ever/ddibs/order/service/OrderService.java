package io.github.park4ever.ddibs.order.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.dto.CreateOrderResponse;
import io.github.park4ever.ddibs.order.dto.OrderResponse;
import io.github.park4ever.ddibs.order.dto.OrderSummaryResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final String ORDER_CODE_PREFIX = "ORD-";
    private static final String ORDER_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ORDER_CODE_LENGTH = 8;
    private static final int MAX_ORDER_CODE_RETRY_COUNT = 10;
    private static final int ORDER_QUANTITY = 1;
    private static final long HOLD_TTL_MINUTES = 10L;

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final LaunchVariantRepository launchVariantRepository;
    private final HoldReservationRepository holdReservationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreateOrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        Member member = findMemberById(memberId);
        LaunchVariant launchVariant = findLaunchVariantForUpdate(request.launchVariantId());

        validateLaunchVariantOrderable(launchVariant);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_TTL_MINUTES);

        for (int attempt = 0; attempt < MAX_ORDER_CODE_RETRY_COUNT; attempt++) {
            String orderCode = generateOrderCode();

            if (orderRepository.existsByOrderCode(orderCode)) {
                continue;
            }

            Order order = Order.create(
                    member,
                    launchVariant.getLaunch().getProduct().getSeller().getId(),
                    launchVariant,
                    orderCode,
                    launchVariant.getLaunch().getProduct().getName(),
                    launchVariant.getProductVariant().getName(),
                    launchVariant.getSalePrice(),
                    ORDER_QUANTITY
            );

            try {
                Order savedOrder = orderRepository.save(order);

                HoldReservation holdReservation = HoldReservation.create(savedOrder, expiresAt);
                holdReservationRepository.save(holdReservation);

                launchVariant.decreaseAvailableStock(ORDER_QUANTITY);

                return CreateOrderResponse.from(savedOrder);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_ORDER_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.ORDER_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.ORDER_CODE_GENERATION_FAILED);
    }

    public OrderResponse getMyOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return OrderResponse.from(order);
    }

    public List<OrderSummaryResponse> getMyOrders(Long memberId) {
        return orderRepository.findAllByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private LaunchVariant findLaunchVariantForUpdate(Long launchVariantId) {
        return launchVariantRepository.findByIdForUpdate(launchVariantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));
    }

    private void validateLaunchVariantOrderable(LaunchVariant launchVariant) {
        LocalDateTime now = LocalDateTime.now();

        if (!launchVariant.getLaunch().isOrderableAt(now)) {
            throw new BusinessException(ErrorCode.ORDER_CREATION_NOT_ALLOWED);
        }

        if (!launchVariant.hasAvailableStock(ORDER_QUANTITY)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_LAUNCH_VARIANT_STOCK);
        }
    }

    private String generateOrderCode() {
        StringBuilder builder = new StringBuilder(ORDER_CODE_PREFIX);

        for (int idx = 0; idx < ORDER_CODE_LENGTH; idx++) {
            int randomIdx = secureRandom.nextInt(ORDER_CODE_CHARACTERS.length());
            builder.append(ORDER_CODE_CHARACTERS.charAt(randomIdx));
        }

        return builder.toString();
    }
}
