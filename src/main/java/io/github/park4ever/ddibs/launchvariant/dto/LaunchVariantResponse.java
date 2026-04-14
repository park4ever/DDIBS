package io.github.park4ever.ddibs.launchvariant.dto;

import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LaunchVariantResponse(
        Long id,
        Long launchId,
        Long productVariantId,
        BigDecimal salePrice,
        int totalStock,
        int availableStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LaunchVariantResponse from(LaunchVariant launchVariant) {
        return new LaunchVariantResponse(
                launchVariant.getId(),
                launchVariant.getLaunch().getId(),
                launchVariant.getProductVariant().getId(),
                launchVariant.getSalePrice(),
                launchVariant.getTotalStock(),
                launchVariant.getAvailableStock(),
                launchVariant.getCreatedAt(),
                launchVariant.getUpdatedAt()
        );
    }
}
