package zw.codinho.ridehail.driver.rest;

import zw.codinho.ridehail.driver.domain.DriverStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        String licenseNumber,
        DriverStatus status,
        BigDecimal rating,
        BigDecimal walletBalance,
        boolean blocked,
        String blockedReason,
        OffsetDateTime blockedAt,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        VehicleResponse vehicle,
        OffsetDateTime createdAt
) {
}
