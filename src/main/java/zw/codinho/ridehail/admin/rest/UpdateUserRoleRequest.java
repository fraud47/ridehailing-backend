package zw.codinho.ridehail.admin.rest;

import jakarta.validation.constraints.NotNull;
import zw.codinho.ridehail.auth.domain.UserRole;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
