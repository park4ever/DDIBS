package io.github.park4ever.ddibs.launchvariant.dto;

import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;

import java.math.BigDecimal;

public record LaunchVariantSummaryResponse(
        Long id,
        Long launchId,
        Long productVariantId,
        BigDecimal salePrice,
        int totalStock,
        int availableStock
) {
    public static LaunchVariantSummaryResponse from(LaunchVariant launchVariant) {
        return new LaunchVariantSummaryResponse(
                launchVariant.getId(),
                launchVariant.getLaunch().getId(),
                launchVariant.getProductVariant().getId(),
                launchVariant.getSalePrice(),
                launchVariant.getTotalStock(),
                launchVariant.getAvailableStock()
        );
    }
}
