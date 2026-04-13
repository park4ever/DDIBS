package io.github.park4ever.ddibs.productvariant.dto;

import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariantStatus;

public record CreateProductVariantResponse(
        Long id,
        Long productId,
        String variantCode,
        String name,
        ProductVariantStatus status
) {
    public static CreateProductVariantResponse from(ProductVariant productVariant) {
        return new CreateProductVariantResponse(
                productVariant.getId(),
                productVariant.getProduct().getId(),
                productVariant.getVariantCode(),
                productVariant.getName(),
                productVariant.getStatus()
        );
    }
}
