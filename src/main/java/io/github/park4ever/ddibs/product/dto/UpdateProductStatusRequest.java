package io.github.park4ever.ddibs.product.dto;

import io.github.park4ever.ddibs.product.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "상품 상태는 필수값입니다.")
        ProductStatus status
) {
}
