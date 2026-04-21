package io.github.park4ever.ddibs.settlement.dto;

import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        Long orderId,
        Long sellerId,
        String settlementCode,
        BigDecimal settlementAmount,
        SettlementStatus status,
        LocalDateTime settledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getOrder().getId(),
                settlement.getSellerId(),
                settlement.getSettlementCode(),
                settlement.getSettlementAmount(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}
