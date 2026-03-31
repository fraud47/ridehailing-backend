package zw.codinho.ridehail.admin.rest;

import jakarta.validation.constraints.NotBlank;

public record BlockDriverRequest(
        @NotBlank(message = "Block reason is required")
        String reason
) {
}
