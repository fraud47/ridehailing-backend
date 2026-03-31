package zw.codinho.ridehail.ride.rest;

import zw.codinho.ridehail.ride.domain.RideStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RideResponse(
        UUID id,
        UUID riderId,
        String riderName,
        UUID driverId,
        String driverName,
        RideStatus status,
        String pickupAddress,
        String dropoffAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude,
        BigDecimal dropoffLatitude,
        BigDecimal dropoffLongitude,
        BigDecimal distanceInKm,
        BigDecimal quotedFare,
        BigDecimal actualFare,
        OffsetDateTime requestedAt,
        OffsetDateTime assignedAt,
        OffsetDateTime pickedUpAt,
        OffsetDateTime completedAt
) {
}
