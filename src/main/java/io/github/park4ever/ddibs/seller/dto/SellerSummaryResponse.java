package io.github.park4ever.ddibs.seller.dto;

import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.domain.SellerStatus;

public record SellerSummaryResponse(
        Long id,
        String sellerCode,
        String name,
        SellerStatus status
) {
    public static SellerSummaryResponse from(Seller seller) {
        return new SellerSummaryResponse(seller.getId(),
                seller.getSellerCode(),
                seller.getName(),
                seller.getStatus());
    }
}
