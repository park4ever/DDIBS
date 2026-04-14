package io.github.park4ever.ddibs.launchvariant.dto;

import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;

import java.math.BigDecimal;

public record CreateLaunchVariantResponse(
        Long id,
        Long launchId,
        Long productVariantId,
        BigDecimal salePrice,
        int totalStock,
        int availableStock
) {
    public static CreateLaunchVariantResponse from(LaunchVariant launchVariant) {
        return new CreateLaunchVariantResponse(
                launchVariant.getId(),
                launchVariant.getLaunch().getId(),
                launchVariant.getProductVariant().getId(),
                launchVariant.getSalePrice(),
                launchVariant.getTotalStock(),
                launchVariant.getAvailableStock()
        );
    }
}
