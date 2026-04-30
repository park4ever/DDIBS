package io.github.park4ever.ddibs.order.dto.admin;

import io.github.park4ever.ddibs.order.domain.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AdminOrderSearchRequest(
        String orderCode,
        OrderStatus status,
        Long sellerId,
        Long memberId,
        String memberEmailKeyword,
        String productNameKeyword,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to
) {
}
