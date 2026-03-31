package zw.codinho.ridehail.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google ID token is required")
        String idToken,
        String phoneNumber
) {
}
