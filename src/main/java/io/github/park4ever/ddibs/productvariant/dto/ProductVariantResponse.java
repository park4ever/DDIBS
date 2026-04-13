package io.github.park4ever.ddibs.productvariant.dto;

import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariantStatus;

import java.time.LocalDateTime;

public record ProductVariantResponse(
        Long id,
        Long productId,
        String variantCode,
        String name,
        ProductVariantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductVariantResponse from(ProductVariant productVariant) {
        return new ProductVariantResponse(
                productVariant.getId(),
                productVariant.getProduct().getId(),
                productVariant.getVariantCode(),
                productVariant.getName(),
                productVariant.getStatus(),
                productVariant.getCreatedAt(),
                productVariant.getUpdatedAt()
        );
    }
}
