package zw.codinho.ridehail.admin.rest;

import jakarta.validation.constraints.NotEmpty;
import zw.codinho.ridehail.auth.domain.UserRole;

import java.util.Set;

public record UpdateUserRoleRequest(
        @NotEmpty(message = "At least one role is required")
        Set<UserRole> roles
) {
}
