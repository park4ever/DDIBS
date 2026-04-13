package io.github.park4ever.ddibs.product.dto;

import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.domain.ProductStatus;

public record CreateProductResponse(
        Long id,
        Long sellerId,
        String productCode,
        String name,
        ProductStatus status
) {
    public static CreateProductResponse from(Product product) {
        return new CreateProductResponse(
                product.getId(),
                product.getSeller().getId(),
                product.getProductCode(),
                product.getName(),
                product.getStatus()
        );
    }
}
