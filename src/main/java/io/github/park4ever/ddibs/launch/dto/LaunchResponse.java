package io.github.park4ever.ddibs.launch.dto;

import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.domain.LaunchStatus;

import java.time.LocalDateTime;

public record LaunchResponse(
        Long id,
        Long productId,
        String launchCode,
        String name,
        LaunchStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LaunchResponse from(Launch launch) {
        return new LaunchResponse(
                launch.getId(),
                launch.getProduct().getId(),
                launch.getLaunchCode(),
                launch.getName(),
                launch.getStatus(),
                launch.getStartAt(),
                launch.getEndAt(),
                launch.getCreatedAt(),
                launch.getUpdatedAt()
        );
    }
}
