package io.github.park4ever.ddibs.holdreservation.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "hold_reservation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hold_reservation_order_id", columnNames = "order_id")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class HoldReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private HoldReservation(Order order, int quantity, HoldStatus status, LocalDateTime expiresAt) {
        validateOrder(order);
        validateOrderStatus(order);
        validateQuantity(quantity);
        validateExpiresAt(expiresAt);

        this.order = order;
        this.quantity = quantity;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static HoldReservation create(Order order, LocalDateTime expiresAt) {
        return new HoldReservation(
                order,
                order.getQuantity(),
                HoldStatus.ACTIVE,
                expiresAt
        );
    }

    public void consume() {
        if (this.status != HoldStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_STATUS_TRANSITION);
        }

        this.status = HoldStatus.CONSUMED;
    }

    public void cancel() {
        if (this.status != HoldStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_STATUS_TRANSITION);
        }

        this.status = HoldStatus.CANCELLED;
    }

    public void expire() {
        if (this.status != HoldStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_STATUS_TRANSITION);
        }

        this.status = HoldStatus.EXPIRED;
    }

    public boolean isActive() {
        return this.status == HoldStatus.ACTIVE;
    }

    public boolean isExpiredAt(LocalDateTime currentTime) {
        return this.status == HoldStatus.ACTIVE
                && currentTime != null
                && currentTime.isAfter(this.expiresAt);
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_ORDER);
        }
    }

    private void validateOrderStatus(Order order) {
        if (!order.isCreated()) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_ORDER_STATUS);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_QUANTITY);
        }
    }

    private void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_HOLD_EXPIRES_AT);
        }
    }
}
