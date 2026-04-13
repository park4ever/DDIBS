package io.github.park4ever.ddibs.productvariant.dto;

import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariantStatus;

public record ProductVariantSummaryResponse(
        Long id,
        Long productId,
        String variantCode,
        String name,
        ProductVariantStatus status
) {
    public static ProductVariantSummaryResponse from(ProductVariant productVariant) {
        return new ProductVariantSummaryResponse(
                productVariant.getId(),
                productVariant.getProduct().getId(),
                productVariant.getVariantCode(),
                productVariant.getName(),
                productVariant.getStatus()
        );
    }
}
