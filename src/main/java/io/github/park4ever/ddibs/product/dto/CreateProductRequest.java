package io.github.park4ever.ddibs.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotNull(message = "판매자 ID는 필수값입니다.")
        Long sellerId,

        @NotBlank(message = "상품명은 필수값입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name
) {
}
