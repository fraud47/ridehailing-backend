package zw.codinho.ridehail.rider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.rider.rest.CreateRiderRequest;
import zw.codinho.ridehail.rider.rest.RiderResponse;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/riders")
@Tag(name = "Riders", description = "Rider onboarding and retrieval endpoints")
public class RiderController {

    private final RiderService riderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Create rider", description = "Registers a new rider profile")
    public ResponseEntity<ApiResponse<RiderResponse>> createRider(@Valid @RequestBody CreateRiderRequest request) {
        return ApiResponseFactory.created("Rider registered successfully", riderService.createRider(request));
    }

    @GetMapping("/{riderId}")
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "Get rider", description = "Fetches a rider by identifier")
    public ResponseEntity<ApiResponse<RiderResponse>> getRider(@PathVariable UUID riderId) {
        return ApiResponseFactory.ok("Rider fetched successfully", riderService.getRider(riderId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.DISPATCHER + "', '" + AuthRoles.ADMIN + "')")
    @Operation(summary = "List riders", description = "Lists all riders")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> getRiders() {
        return ApiResponseFactory.ok("Riders fetched successfully", riderService.getRiders());
    }
}
