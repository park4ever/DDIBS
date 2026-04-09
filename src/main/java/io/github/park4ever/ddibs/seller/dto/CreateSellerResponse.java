package io.github.park4ever.ddibs.seller.dto;

import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.domain.SellerStatus;

public record CreateSellerResponse(
        Long id,
        String sellerCode,
        String name,
        SellerStatus status
) {
    public static CreateSellerResponse from(Seller seller) {
        return new CreateSellerResponse(
                seller.getId(),
                seller.getSellerCode(),
                seller.getName(),
                seller.getStatus()
        );
    }
}
