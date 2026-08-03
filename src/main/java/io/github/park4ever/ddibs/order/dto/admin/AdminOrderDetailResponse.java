package io.github.park4ever.ddibs.order.dto.admin;

import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.payment.domain.Payment;
import io.github.park4ever.ddibs.settlement.domain.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderDetailResponse(
        Long orderId,
        String orderCode,
        String orderStatus,
        Long memberId,
        String memberName,
        String memberEmail,
        Long sellerId,
        String productName,
        String variantName,
        Integer quantity,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        HoldDetail hold,
        PaymentDetail payment,
        SettlementDetail settlement
) {

    public static AdminOrderDetailResponse of(
            Long orderId,
            String orderCode,
            String orderStatus,
            Long memberId,
            String memberName,
            String memberEmail,
            Long sellerId,
            String productName,
            String variantName,
            Integer quantity,
            BigDecimal totalPrice,
            LocalDateTime createdAt,
            HoldReservation holdReservation,
            Payment payment,
            Settlement settlement
    ) {
        return new AdminOrderDetailResponse(
                orderId,
                orderCode,
                orderStatus,
                memberId,
                memberName,
                memberEmail,
                sellerId,
                productName,
                variantName,
                quantity,
                totalPrice,
                createdAt,
                HoldDetail.from(holdReservation),
                PaymentDetail.from(payment),
                SettlementDetail.from(settlement)
        );
    }

    public record HoldDetail(
            Long holdId,
            String holdStatus,
            Integer quantity,
            LocalDateTime expiresAt
    ) {
        public static HoldDetail from(HoldReservation holdReservation) {
            if (holdReservation == null) {
                return null;
            }

            return new HoldDetail(
                    holdReservation.getId(),
                    holdReservation.getStatus().name(),
                    holdReservation.getQuantity(),
                    holdReservation.getExpiresAt()
            );
        }
    }

    public record PaymentDetail(
            Long paymentId,
            String paymentCode,
            String paymentStatus,
            BigDecimal amount,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            LocalDateTime failedAt,
            String failureReason
    ) {
        public static PaymentDetail from(Payment payment) {
            if (payment == null) {
                return null;
            }

            return new PaymentDetail(
                    payment.getId(),
                    payment.getPaymentCode(),
                    payment.getStatus().name(),
                    payment.getAmount(),
                    payment.getRequestedAt(),
                    payment.getApprovedAt(),
                    payment.getFailedAt(),
                    payment.getFailureReason()
            );
        }
    }

    public record SettlementDetail(
            Long settlementId,
            String settlementCode,
            String settlementStatus,
            BigDecimal settlementAmount,
            LocalDateTime settledAt
    ) {
        public static SettlementDetail from(Settlement settlement) {
            if (settlement == null) {
                return null;
            }

            return new SettlementDetail(
                    settlement.getId(),
                    settlement.getSettlementCode(),
                    settlement.getStatus().name(),
                    settlement.getSettlementAmount(),
                    settlement.getSettledAt()
            );
        }
    }
}