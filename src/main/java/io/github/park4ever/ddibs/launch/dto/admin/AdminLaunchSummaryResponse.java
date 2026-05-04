package io.github.park4ever.ddibs.launch.dto.admin;

import io.github.park4ever.ddibs.launch.domain.LaunchStatus;

import java.time.LocalDateTime;

public record AdminLaunchSummaryResponse(
        Long id,
        String launchCode,
        String launchName,
        LaunchStatus status,
        Long sellerId,
        String sellerName,
        Long productId,
        String productName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long variantCount,
        Long totalStock,
        Long availableStock
) {
}
