package io.github.park4ever.ddibs.settlement.dto;

import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSettlementStatusRequest(
        @NotNull(message = "정산 상태는 필수값입니다.")
        SettlementStatus status
) {
}
