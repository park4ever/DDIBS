package io.github.park4ever.ddibs.product.dto;

import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.domain.ProductStatus;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long sellerId,
        String productCode,
        String name,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSeller().getId(),
                product.getProductCode(),
                product.getName(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
