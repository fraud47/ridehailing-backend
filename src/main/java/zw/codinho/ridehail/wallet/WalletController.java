package zw.codinho.ridehail.wallet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.admin.rest.WalletBalanceResponse;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;
import zw.codinho.ridehail.wallet.domain.WalletOwnerType;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Independent rider and driver wallet endpoints")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/riders/{riderId}")
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Get rider wallet", description = "Returns the rider wallet balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getRiderWallet(@PathVariable UUID riderId) {
        return ApiResponseFactory.ok("Rider wallet fetched successfully", walletService.getWallet(WalletOwnerType.RIDER, riderId));
    }

    @GetMapping("/drivers/{driverId}")
    @PreAuthorize("hasAnyRole('" + AuthRoles.DRIVER + "', '" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Get driver wallet", description = "Returns the driver wallet balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getDriverWallet(@PathVariable UUID driverId) {
        return ApiResponseFactory.ok("Driver wallet fetched successfully", walletService.getWallet(WalletOwnerType.DRIVER, driverId));
    }
}
