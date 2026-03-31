package zw.codinho.ridehail.driver;

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
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.driver.rest.AssignVehicleRequest;
import zw.codinho.ridehail.driver.rest.CreateDriverRequest;
import zw.codinho.ridehail.driver.rest.DriverResponse;
import zw.codinho.ridehail.driver.rest.UpdateDriverAvailabilityRequest;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers", description = "Driver onboarding, vehicles, and availability endpoints")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Create driver", description = "Registers a new driver profile")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        return ApiResponseFactory.created("Driver registered successfully", driverService.createDriver(request));
    }

    @PostMapping("/{driverId}/vehicle")
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Assign vehicle", description = "Registers or updates a driver's vehicle")
    public ResponseEntity<ApiResponse<DriverResponse>> assignVehicle(@PathVariable UUID driverId,
                                                                     @Valid @RequestBody AssignVehicleRequest request) {
        return ApiResponseFactory.ok("Vehicle assigned successfully", driverService.assignVehicle(driverId, request));
    }

    @PatchMapping("/{driverId}/availability")
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Update driver availability", description = "Marks a driver available or offline")
    public ResponseEntity<ApiResponse<DriverResponse>> updateAvailability(@PathVariable UUID driverId,
                                                                          @Valid @RequestBody UpdateDriverAvailabilityRequest request) {
        return ApiResponseFactory.ok("Driver availability updated successfully", driverService.updateAvailability(driverId, request));
    }

    @GetMapping("/{driverId}")
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Get driver", description = "Fetches a driver by identifier")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriver(@PathVariable UUID driverId) {
        return ApiResponseFactory.ok("Driver fetched successfully", driverService.getDriver(driverId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "List drivers", description = "Lists all drivers")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getDrivers() {
        return ApiResponseFactory.ok("Drivers fetched successfully", driverService.getDrivers());
    }
}
