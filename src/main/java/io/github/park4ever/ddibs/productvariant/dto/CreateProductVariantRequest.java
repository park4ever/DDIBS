package io.github.park4ever.ddibs.productvariant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductVariantRequest(
        @NotNull(message = "상품 ID는 필수값입니다.")
        Long productId,

        @NotBlank(message = "상품 Variant명은 필수값입니다.")
        @Size(max = 100, message = "상품 Variant명은 100자 이하여야 합니다.")
        String name
) {
}
