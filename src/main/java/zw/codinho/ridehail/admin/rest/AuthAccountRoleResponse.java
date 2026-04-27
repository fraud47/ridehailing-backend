package zw.codinho.ridehail.admin.rest;

import zw.codinho.ridehail.auth.domain.UserRole;

import java.util.UUID;
import java.util.List;

public record AuthAccountRoleResponse(
        UUID authAccountId,
        String emailAddress,
        List<UserRole> roles
) {
}
