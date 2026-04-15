package io.github.park4ever.ddibs.order.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_code", columnNames = "order_code")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "launch_variant_id", nullable = false)
    private LaunchVariant launchVariant;

    @Column(name = "order_code", nullable = false, length = 20)
    private String orderCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    private Order(
            Member member, Long sellerId, LaunchVariant launchVariant,
            String orderCode, String productName, String variantName,
            BigDecimal unitPrice, int quantity, OrderStatus status
    ) {
        validateMember(member);
        validateSellerId(sellerId);
        validateLaunchVariant(launchVariant);
        validateOrderCode(orderCode);
        validateSnapshot(productName, variantName);
        validateUnitPrice(unitPrice);
        validateQuantity(quantity);

        this.member = member;
        this.sellerId = sellerId;
        this.launchVariant = launchVariant;
        this.orderCode = orderCode;
        this.productName = productName;
        this.variantName = variantName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.status = status;
    }

    public static Order create(
            Member member, Long sellerId, LaunchVariant launchVariant,
            String orderCode, String productName, String variantName,
            BigDecimal unitPrice, int quantity
    ) {
        return new Order(
                member, sellerId, launchVariant, orderCode, productName,
                variantName, unitPrice, quantity, OrderStatus.CREATED
        );
    }

    public void confirm() {
        if (this.status != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        this.status = OrderStatus.CONFIRMED;
    }

    public void failPayment() {
        if (this.status != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void expireHold() {
        if (this.status != OrderStatus.CREATED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        this.status = OrderStatus.HOLD_EXPIRED;
    }

    public boolean isCreated() {
        return this.status == OrderStatus.CREATED;
    }

    public boolean isConfirmed() {
        return this.status == OrderStatus.CONFIRMED;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_MEMBER);
        }
    }

    private void validateSellerId(Long sellerId) {
        if (sellerId == null) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_SELLER);
        }
    }

    private void validateLaunchVariant(LaunchVariant launchVariant) {
        if (launchVariant == null) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_LAUNCH_VARIANT);
        }
    }

    private void validateOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_CODE);
        }
    }

    private void validateSnapshot(String productName, String variantName) {
        if (productName == null || productName.isBlank()
                || variantName == null || variantName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_SNAPSHOT);
        }
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_UNIT_PRICE);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }

        if (quantity != 1) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }
    }
}
