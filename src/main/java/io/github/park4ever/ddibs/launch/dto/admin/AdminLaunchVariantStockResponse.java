package io.github.park4ever.ddibs.launch.dto.admin;

import java.math.BigDecimal;

public record AdminLaunchVariantStockResponse(
        Long launchVariantId,
        Long productVariantId,
        String variantName,
        BigDecimal salePrice,
        int totalStock,
        int availableStock
) {
}
