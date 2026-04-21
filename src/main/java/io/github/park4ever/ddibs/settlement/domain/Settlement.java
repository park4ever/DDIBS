package io.github.park4ever.ddibs.settlement.domain;

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
        name = "settlement",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_settlement_order_id", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_settlement_code", columnNames = "settlement_code")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "settlement_code", nullable = false, length = 20)
    private String settlementCode;

    @Column(name = "settlement_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    private Settlement(
            Order order, Long sellerId, String settlementCode,
            BigDecimal settlementAmount, SettlementStatus status
    ) {
        validateOrder(order);
        validateOrderStatus(order);
        validateSellerId(sellerId);
        validateSettlementCode(settlementCode);
        validateSettlementAmount(settlementAmount);

        this.order = order;
        this.sellerId = sellerId;
        this.settlementCode = settlementCode;
        this.settlementAmount = settlementAmount;
        this.status = status;
        this.settledAt = null;
    }

    public static Settlement create(Order order, String settlementCode) {
        return new Settlement(
                order,
                order.getSellerId(),
                settlementCode,
                order.getTotalPrice(),
                SettlementStatus.CREATED
        );
    }

    public void confirm(LocalDateTime settledAt) {
        if (this.status != SettlementStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        validateSettledAt(settledAt);

        this.status = SettlementStatus.CONFIRMED;
        this.settledAt = settledAt;
    }

    public boolean isCreated() {
        return this.status == SettlementStatus.CREATED;
    }

    public boolean isConfirmed() {
        return this.status == SettlementStatus.CONFIRMED;
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_ORDER);
        }
    }

    private void validateOrderStatus(Order order) {
        if (!order.isConfirmed()) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_ORDER_STATUS);
        }
    }

    private void validateSellerId(Long sellerId) {
        if (sellerId == null) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_SELLER);
        }
    }

    private void validateSettlementCode(String settlementCode) {
        if (settlementCode == null || settlementCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_CODE);
        }
    }

    private void validateSettlementAmount(BigDecimal settlementAmount) {
        if (settlementAmount == null || settlementAmount.signum() < 0) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }
    }

    private void validateSettledAt(LocalDateTime settledAt) {
        if (settledAt == null) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_CONFIRMED_AT);
        }

        if (this.getCreatedAt() != null && settledAt.isBefore(this.getCreatedAt())) {
            throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_CONFIRMED_AT);
        }
    }
}
