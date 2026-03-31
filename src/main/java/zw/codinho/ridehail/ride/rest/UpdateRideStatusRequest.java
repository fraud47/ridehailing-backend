package zw.codinho.ridehail.ride.rest;

import jakarta.validation.constraints.NotNull;
import zw.codinho.ridehail.ride.domain.RideStatus;

public record UpdateRideStatusRequest(
        @NotNull(message = "Ride status is required")
        RideStatus status
) {
}
