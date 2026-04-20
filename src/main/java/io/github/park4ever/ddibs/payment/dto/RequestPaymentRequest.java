package io.github.park4ever.ddibs.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestPaymentRequest(
        @NotNull(message = "주문 ID는 필수값입니다.")
        Long orderId,

        @NotNull(message = "모킹 결제 결과는 필수값입니다.")
        Boolean mockSuccess,

        @Size(max = 255, message = "결제 실패 사유는 255자 이하여야 합니다.")
        String failureReason
) {
}
