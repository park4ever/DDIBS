package io.github.park4ever.ddibs.seller.dto;

import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.domain.SellerStatus;

import java.time.LocalDateTime;

public record SellerResponse(
        Long id,
        String sellerCode,
        String name,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getSellerCode(),
                seller.getName(),
                seller.getStatus(),
                seller.getCreatedAt(),
                seller.getUpdatedAt()
        );
    }
}
