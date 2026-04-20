package io.github.park4ever.ddibs.payment.dto;

import io.github.park4ever.ddibs.payment.domain.Payment;
import io.github.park4ever.ddibs.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RequestPaymentResponse(
        Long id,
        Long orderId,
        String paymentCode,
        BigDecimal amount,
        PaymentStatus status,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt,
        String failureReason
) {
    public static RequestPaymentResponse from(Payment payment) {
        return new RequestPaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentCode(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getRequestedAt(),
                payment.getApprovedAt(),
                payment.getFailedAt(),
                payment.getFailureReason()
        );
    }
}
