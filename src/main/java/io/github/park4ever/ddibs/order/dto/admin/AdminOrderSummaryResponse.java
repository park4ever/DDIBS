package io.github.park4ever.ddibs.order.dto.admin;

import io.github.park4ever.ddibs.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderSummaryResponse(
        Long id,
        String orderCode,
        Long memberId,
        String memberEmail,
        String memberName,
        Long sellerId,
        String productName,
        String variantName,
        int quantity,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
