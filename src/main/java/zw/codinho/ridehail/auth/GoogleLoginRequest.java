package zw.codinho.ridehail.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import zw.codinho.ridehail.auth.domain.UserRole;

import java.util.Set;

public record GoogleLoginRequest(
        @NotBlank(message = "Google ID token is required")
        String idToken,
        String phoneNumber,
        Set<UserRole> requestedRoles,
        @Valid GoogleDriverProfileRequest driver
) {
}
