package io.github.park4ever.ddibs.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "발매 Variant ID는 필수값입니다.")
        Long launchVariantId
) {
}
