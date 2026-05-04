package io.github.park4ever.ddibs.settlement.dto.admin;

import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AdminSettlementSearchRequest(
        String settlementCode,
        String orderCode,
        Long sellerId,
        SettlementStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to
) {
}
