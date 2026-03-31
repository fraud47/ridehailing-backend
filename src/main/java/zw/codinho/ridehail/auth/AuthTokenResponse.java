package zw.codinho.ridehail.auth;

import zw.codinho.ridehail.rider.rest.RiderResponse;

import java.time.OffsetDateTime;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        boolean newUser,
        RiderResponse rider
) {
}
