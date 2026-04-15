package io.github.park4ever.ddibs.order.dto;

import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderCode,
        Long launchVariantId,
        Long sellerId,
        String productName,
        String variantName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getLaunchVariant().getId(),
                order.getSellerId(),
                order.getProductName(),
                order.getVariantName(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
