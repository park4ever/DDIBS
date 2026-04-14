package io.github.park4ever.ddibs.launchvariant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateLaunchVariantRequest(
        @NotNull(message = "발매 ID는 필수값입니다.")
        Long launchId,

        @NotNull(message = "상품 Variant ID는 필수값입니다.")
        Long productVariantId,

        @NotNull(message = "발매가는 필수값입니다.")
        @DecimalMin(value = "0.00", inclusive = true, message = "발매가는 0 이상이어야 합니다.")
        BigDecimal salePrice,

        @NotNull(message = "총 재고는 필수값입니다.")
        @PositiveOrZero(message = "총 재고는 0 이상이어야 합니다.")
        Integer totalStock
) {
}
