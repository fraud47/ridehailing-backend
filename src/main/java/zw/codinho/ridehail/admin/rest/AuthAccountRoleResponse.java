package zw.codinho.ridehail.admin.rest;

import zw.codinho.ridehail.auth.domain.UserRole;

import java.util.UUID;

public record AuthAccountRoleResponse(
        UUID authAccountId,
        String emailAddress,
        UserRole role
) {
}
