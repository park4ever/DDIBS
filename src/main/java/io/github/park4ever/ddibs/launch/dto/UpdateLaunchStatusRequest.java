package io.github.park4ever.ddibs.launch.dto;

import io.github.park4ever.ddibs.launch.domain.LaunchStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLaunchStatusRequest(
        @NotNull(message = "발매 상태는 필수값입니다.")
        LaunchStatus status
) {
}
