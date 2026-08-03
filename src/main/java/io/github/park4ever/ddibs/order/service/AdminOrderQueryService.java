package io.github.park4ever.ddibs.order.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderDetailResponse;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.payment.domain.Payment;
import io.github.park4ever.ddibs.payment.repository.PaymentRepository;
import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderQueryService {

    private final OrderRepository orderRepository;
    private final HoldReservationRepository holdReservationRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    public Page<AdminOrderSummaryResponse> getOrders(
            AdminOrderSearchRequest condition,
            Pageable pageable
    ) {
        return orderRepository.searchAdminOrders(condition, pageable);
    }

    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        HoldReservation holdReservation = holdReservationRepository.findByOrderId(orderId)
                .orElse(null);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        Settlement settlement = settlementRepository.findByOrderId(orderId)
                .orElse(null);

        return AdminOrderDetailResponse.of(
                order.getId(),
                order.getOrderCode(),
                order.getStatus().name(),
                order.getMember().getId(),
                order.getMember().getName(),
                order.getMember().getEmail(),
                order.getSellerId(),
                order.getProductName(),
                order.getVariantName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                holdReservation,
                payment,
                settlement
        );
    }
}
