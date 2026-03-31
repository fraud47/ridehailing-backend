package zw.codinho.ridehail.ride.rest;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRideRequest(
        @NotNull(message = "Rider identifier is required")
        UUID riderId,
        @NotBlank(message = "Pickup address is required")
        String pickupAddress,
        @NotBlank(message = "Dropoff address is required")
        String dropoffAddress,
        @NotNull(message = "Pickup latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal pickupLatitude,
        @NotNull(message = "Pickup longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal pickupLongitude,
        @NotNull(message = "Dropoff latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal dropoffLatitude,
        @NotNull(message = "Dropoff longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal dropoffLongitude
) {
}
