package io.github.park4ever.ddibs.launch.dto.admin;

import io.github.park4ever.ddibs.launch.domain.LaunchStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AdminLaunchSearchRequest(
        String launchCode,
        LaunchStatus status,
        Long sellerId,
        String productNameKeyword,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to
) {
}
