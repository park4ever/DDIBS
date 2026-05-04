package io.github.park4ever.ddibs.settlement.dto.admin;

import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSettlementSummaryResponse(
        Long id,
        Long orderId,
        String orderCode,
        Long sellerId,
        String settlementCode,
        BigDecimal settlementAmount,
        SettlementStatus status,
        LocalDateTime settledAt,
        LocalDateTime createdAt
) {
}
