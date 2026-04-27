package zw.codinho.ridehail.auth;

import zw.codinho.ridehail.auth.domain.UserRole;
import zw.codinho.ridehail.driver.rest.DriverResponse;
import zw.codinho.ridehail.rider.rest.RiderResponse;

import java.time.OffsetDateTime;
import java.util.List;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        boolean newUser,
        List<UserRole> roles,
        RiderResponse rider,
        DriverResponse driver
) {
}
