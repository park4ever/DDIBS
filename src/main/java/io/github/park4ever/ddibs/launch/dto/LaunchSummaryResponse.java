package io.github.park4ever.ddibs.launch.dto;

import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.domain.LaunchStatus;

import java.time.LocalDateTime;

public record LaunchSummaryResponse(
        Long id,
        Long productId,
        String launchCode,
        String name,
        LaunchStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static LaunchSummaryResponse from(Launch launch) {
        return new LaunchSummaryResponse(
                launch.getId(),
                launch.getProduct().getId(),
                launch.getLaunchCode(),
                launch.getName(),
                launch.getStatus(),
                launch.getStartAt(),
                launch.getEndAt()
        );
    }
}
