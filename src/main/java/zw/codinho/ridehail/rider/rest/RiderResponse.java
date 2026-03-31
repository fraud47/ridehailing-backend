package zw.codinho.ridehail.rider.rest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RiderResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        String emailAddress,
        BigDecimal rating,
        BigDecimal walletBalance,
        OffsetDateTime createdAt
) {
}
