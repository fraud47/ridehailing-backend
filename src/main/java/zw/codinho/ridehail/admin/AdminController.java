package zw.codinho.ridehail.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.admin.rest.AdminDashboardMetricsResponse;
import zw.codinho.ridehail.admin.rest.AuthAccountRoleResponse;
import zw.codinho.ridehail.admin.rest.BlockDriverRequest;
import zw.codinho.ridehail.admin.rest.DepositFundsRequest;
import zw.codinho.ridehail.admin.rest.PlatformAccountResponse;
import zw.codinho.ridehail.admin.rest.UpdateUserRoleRequest;
import zw.codinho.ridehail.admin.rest.WalletBalanceResponse;
import zw.codinho.ridehail.admin.rest.WithdrawFundsRequest;
import zw.codinho.ridehail.driver.rest.DriverResponse;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Super-user administration, metrics, and earnings endpoints")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/riders/{riderId}/deposit")
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Deposit rider funds", description = "Deposits money into a rider wallet")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> depositFunds(@PathVariable UUID riderId,
                                                                           @Valid @RequestBody DepositFundsRequest request) {
        return ApiResponseFactory.ok("Funds deposited successfully", adminService.depositRiderFunds(riderId, request.amount()));
    }

    @PostMapping("/platform/withdrawals")
    @PreAuthorize("hasRole('" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Withdraw platform earnings", description = "Withdraws accumulated platform commission")
    public ResponseEntity<ApiResponse<PlatformAccountResponse>> withdrawPlatformFunds(@Valid @RequestBody WithdrawFundsRequest request) {
        return ApiResponseFactory.ok("Platform funds withdrawn successfully", adminService.withdrawPlatformFunds(request.amount()));
    }

    @PatchMapping("/drivers/{driverId}/block")
    @PreAuthorize("hasAnyRole('" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Block driver", description = "Blocks a driver from receiving new rides")
    public ResponseEntity<ApiResponse<DriverResponse>> blockDriver(@PathVariable UUID driverId,
                                                                   @Valid @RequestBody BlockDriverRequest request) {
        return ApiResponseFactory.ok("Driver blocked successfully", adminService.blockDriver(driverId, request.reason()));
    }

    @DeleteMapping("/drivers/{driverId}/block")
    @PreAuthorize("hasAnyRole('" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Unblock driver", description = "Removes a driver block")
    public ResponseEntity<ApiResponse<DriverResponse>> unblockDriver(@PathVariable UUID driverId) {
        return ApiResponseFactory.ok("Driver unblocked successfully", adminService.unblockDriver(driverId));
    }

    @GetMapping("/dashboard/metrics")
    @PreAuthorize("hasAnyRole('" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Dashboard metrics", description = "Returns operational metrics for an admin dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardMetricsResponse>> getDashboardMetrics() {
        return ApiResponseFactory.ok("Dashboard metrics fetched successfully", adminService.getDashboardMetrics());
    }

    @PatchMapping({"/auth-accounts/{authAccountId}/role", "/auth-accounts/{authAccountId}/roles"})
    @PreAuthorize("hasRole('" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Update auth account roles", description = "Promotes or demotes an auth account, including the SUPER_USER role")
    public ResponseEntity<ApiResponse<AuthAccountRoleResponse>> updateUserRole(@PathVariable UUID authAccountId,
                                                                               @Valid @RequestBody UpdateUserRoleRequest request) {
        return ApiResponseFactory.ok("User roles updated successfully", adminService.updateUserRoles(authAccountId, request.roles()));
    }
}
