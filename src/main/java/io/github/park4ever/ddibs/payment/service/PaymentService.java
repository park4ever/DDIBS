package io.github.park4ever.ddibs.payment.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.payment.domain.Payment;
import io.github.park4ever.ddibs.payment.dto.PaymentResponse;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentRequest;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentResponse;
import io.github.park4ever.ddibs.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String PAYMENT_CODE_PREFIX = "PAY-";
    private static final String PAYMENT_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PAYMENT_CODE_LENGTH = 8;
    private static final int MAX_PAYMENT_CODE_RETRY_COUNT = 10;
    private static final String DEFAULT_PAYMENT_FAILURE_REASON = "모킹 결제 실패";

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final HoldReservationRepository holdReservationRepository;
    private final LaunchVariantRepository launchVariantRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RequestPaymentResponse requestPayment(Long memberId, RequestPaymentRequest request) {
        Order order = findMyOrder(memberId, request.orderId());

        validatePaymentNotExists(order.getId());

        HoldReservation holdReservation = findHoldByOrderId(order.getId());
        validateHoldIsActive(holdReservation);

        LocalDateTime requestedAt = LocalDateTime.now();

        for (int attempt = 0; attempt < MAX_PAYMENT_CODE_RETRY_COUNT; attempt++) {
            String paymentCode = generatePaymentCode();

            if (paymentRepository.existsByPaymentCode(paymentCode)) {
                continue;
            }

            Payment payment = Payment.create(order, paymentCode, requestedAt);

            try {
                Payment savedPayment = paymentRepository.saveAndFlush(payment);

                if (Boolean.TRUE.equals(request.mockSuccess())) {
                    processPaymentSuccess(savedPayment, order, holdReservation, requestedAt);
                } else {
                    processPaymentFailure(savedPayment, order, holdReservation, request.failureReason(), requestedAt);
                }

                return RequestPaymentResponse.from(savedPayment);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_PAYMENT_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.PAYMENT_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.PAYMENT_CODE_GENERATION_FAILED);
    }

    public PaymentResponse getMyPayment(Long memberId, Long orderId) {
        Order order = findMyOrder(memberId, orderId);

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    private Order findMyOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private HoldReservation findHoldByOrderId(Long orderId) {
        return holdReservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLD_NOT_FOUND));
    }

    private void validatePaymentNotExists(Long orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }

    private void validateHoldIsActive(HoldReservation holdReservation) {
        if (!holdReservation.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_STATUS_TRANSITION);
        }
    }

    private void processPaymentSuccess(
            Payment payment, Order order, HoldReservation holdReservation, LocalDateTime approvedAt
    ) {
        payment.succeed(approvedAt);
        order.confirm();
        holdReservation.consume();
    }

    private void processPaymentFailure(
            Payment payment, Order order,
            HoldReservation holdReservation,
            String failureReason, LocalDateTime failedAt
    ) {
        LaunchVariant launchVariant = launchVariantRepository.findByIdForUpdate(order.getLaunchVariant().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));

        String resolvedFailureReason = resolveFailureReason(failureReason);

        payment.fail(failedAt, resolvedFailureReason);
        order.failPayment();
        holdReservation.cancel();
        launchVariant.restoreAvailableStock(order.getQuantity());
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return DEFAULT_PAYMENT_FAILURE_REASON;
        }

        return failureReason;
    }

    private String generatePaymentCode() {
        StringBuilder builder = new StringBuilder(PAYMENT_CODE_PREFIX);

        for (int idx = 0; idx < PAYMENT_CODE_LENGTH; idx++) {
            int randomIdx = secureRandom.nextInt(PAYMENT_CODE_CHARACTERS.length());
            builder.append(PAYMENT_CODE_CHARACTERS.charAt(randomIdx));
        }

        return builder.toString();
    }
}
