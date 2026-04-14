package io.github.park4ever.ddibs.launch.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateLaunchRequest(
        @NotNull(message = "상품 ID는 필수값입니다.")
        Long productId,

        @NotBlank(message = "발매명은 필수값입니다.")
        @Size(max = 100, message = "발매명은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "발매 시작 시간은 필수값입니다.")
        @Future(message = "발매 시작 시간은 현재보다 미래여야 합니다.")
        LocalDateTime startAt,

        @NotNull(message = "발매 종료 시간은 필수압니다.")
        @Future(message = "발매 종료 시간은 현재보다 미래여야 합니다.")
        LocalDateTime endAt
) {
}
