package io.github.park4ever.ddibs.seller.dto;

import io.github.park4ever.ddibs.seller.domain.SellerStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSellerStatusRequest(
        @NotNull(message = "판매자 상태는 필수값입니다.")
        SellerStatus status
) {
}
