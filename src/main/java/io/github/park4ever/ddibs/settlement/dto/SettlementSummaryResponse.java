package io.github.park4ever.ddibs.settlement.dto;

import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementSummaryResponse(
        Long id,
        Long orderId,
        Long sellerId,
        String settlementCode,
        BigDecimal settlementAmount,
        SettlementStatus status,
        LocalDateTime createdAt
) {
    public static SettlementSummaryResponse from(Settlement settlement) {
        return new SettlementSummaryResponse(
                settlement.getId(),
                settlement.getOrder().getId(),
                settlement.getSellerId(),
                settlement.getSettlementCode(),
                settlement.getSettlementAmount(),
                settlement.getStatus(),
                settlement.getCreatedAt()
        );
    }
}
