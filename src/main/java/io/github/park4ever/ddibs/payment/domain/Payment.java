package io.github.park4ever.ddibs.payment.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_order_id", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_payment_code", columnNames = "payment_code")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_code", nullable = false, length = 20)
    private String paymentCode;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    private Payment(
            Order order, String paymentCode, BigDecimal amount,
            PaymentStatus status, LocalDateTime requestedAt
    ) {
        validateOrder(order);
        validateOrderStatus(order);
        validatePaymentCode(paymentCode);
        validateAmount(amount);
        validateRequestedAt(requestedAt);

        this.order = order;
        this.paymentCode = paymentCode;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public static Payment create(Order order, String paymentCode, LocalDateTime requestedAt) {
        return new Payment(
                order,
                paymentCode,
                order.getTotalPrice(),
                PaymentStatus.PENDING,
                requestedAt
        );
    }

    public void succeed(LocalDateTime approvedAt) {
        if (this.status != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION);
        }

        validateApprovedAt(approvedAt);

        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = approvedAt;
        this.failedAt = null;
        this.failureReason = null;
    }

    public void fail(LocalDateTime failedAt, String failureReason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION);
        }

        validateFailedAt(failedAt);
        validateFailureReason(failureReason);

        this.status = PaymentStatus.FAILED;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.approvedAt = null;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_ORDER);
        }
    }

    private void validateOrderStatus(Order order) {
        if (!order.isCreated()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_ORDER_STATUS);
        }
    }

    private void validatePaymentCode(String paymentCode) {
        if (paymentCode == null || paymentCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_CODE);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private void validateRequestedAt(LocalDateTime requestedAt) {
        if (requestedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUESTED_AT);
        }
    }

    private void validateApprovedAt(LocalDateTime approvedAt) {
        if (approvedAt == null || approvedAt.isBefore(this.requestedAt)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_APPROVED_AT);
        }
    }

    private void validateFailedAt(LocalDateTime failedAt) {
        if (failedAt == null || failedAt.isBefore(this.requestedAt)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_FAILED_AT);
        }
    }

    private void validateFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_FAILURE_REASON);
        }

        if (failureReason.length() > 255) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_FAILURE_REASON);
        }
    }
}
