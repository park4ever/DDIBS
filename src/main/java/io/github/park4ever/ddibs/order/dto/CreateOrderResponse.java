package io.github.park4ever.ddibs.order.dto;

import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long id,
        String orderCode,
        Long launchVariantId,
        Long sellerId,
        String productName,
        String variantName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalPrice,
        OrderStatus status
) {
    public static CreateOrderResponse from(Order order) {
        return new CreateOrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getLaunchVariant().getId(),
                order.getSellerId(),
                order.getProductName(),
                order.getVariantName(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus()
        );
    }
}
