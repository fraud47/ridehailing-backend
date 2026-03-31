package zw.codinho.ridehail.ride;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.ride.rest.CreateRideRequest;
import zw.codinho.ridehail.ride.rest.RideQuoteRequest;
import zw.codinho.ridehail.ride.rest.RideQuoteResponse;
import zw.codinho.ridehail.ride.rest.RideResponse;
import zw.codinho.ridehail.ride.rest.UpdateRideStatusRequest;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rides")
@Tag(name = "Rides", description = "Fare quoting, dispatching, and ride lifecycle endpoints")
public class RideController {

    private final RideService rideService;

    @PostMapping("/quotes")
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Create fare quote", description = "Calculates distance and estimated fare for a route")
    public ResponseEntity<ApiResponse<RideQuoteResponse>> quoteRide(@Valid @RequestBody RideQuoteRequest request) {
        return ApiResponseFactory.ok("Ride quote generated successfully", rideService.quoteRide(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Request a ride", description = "Creates a ride and automatically assigns the nearest available driver")
    public ResponseEntity<ApiResponse<RideResponse>> createRide(@Valid @RequestBody CreateRideRequest request) {
        return ApiResponseFactory.created("Ride requested successfully", rideService.createRide(request));
    }

    @PatchMapping("/{rideId}/status")
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Update ride status", description = "Moves a ride through its lifecycle")
    public ResponseEntity<ApiResponse<RideResponse>> updateRideStatus(@PathVariable UUID rideId,
                                                                      @Valid @RequestBody UpdateRideStatusRequest request) {
        return ApiResponseFactory.ok("Ride status updated successfully", rideService.updateRideStatus(rideId, request.status()));
    }

    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DRIVER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Get ride", description = "Fetches a ride by identifier")
    public ResponseEntity<ApiResponse<RideResponse>> getRide(@PathVariable UUID rideId) {
        return ApiResponseFactory.ok("Ride fetched successfully", rideService.getRide(rideId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "List rides", description = "Lists rides, optionally filtered by rider or driver")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getRides(@RequestParam(required = false) UUID riderId,
                                                                    @RequestParam(required = false) UUID driverId) {
        return ApiResponseFactory.ok("Rides fetched successfully", rideService.getRides(riderId, driverId));
    }
}
