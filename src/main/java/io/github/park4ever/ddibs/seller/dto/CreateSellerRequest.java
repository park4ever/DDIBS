package io.github.park4ever.ddibs.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSellerRequest(
        @NotBlank(message = "판매자명은 필수값입니다.")
        @Size(max = 100, message = "판매자명은 100자 이하여야 합니다.")
        String name
) {
}
