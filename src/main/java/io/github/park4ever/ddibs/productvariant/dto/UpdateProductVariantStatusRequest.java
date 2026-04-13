package io.github.park4ever.ddibs.productvariant.dto;

import io.github.park4ever.ddibs.productvariant.domain.ProductVariantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductVariantStatusRequest(

        @NotNull(message = "상품 Variant 상태는 필수값입니다.")
        ProductVariantStatus status
) {
}