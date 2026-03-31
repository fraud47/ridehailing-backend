package zw.codinho.ridehail.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.admin.rest.AdminDashboardMetricsResponse;
import zw.codinho.ridehail.admin.rest.AuthAccountRoleResponse;
import zw.codinho.ridehail.admin.rest.PlatformAccountResponse;
import zw.codinho.ridehail.auth.domain.AuthAccount;
import zw.codinho.ridehail.auth.domain.AuthAccountRepository;
import zw.codinho.ridehail.auth.domain.UserRole;
import zw.codinho.ridehail.driver.DriverService;
import zw.codinho.ridehail.driver.domain.Driver;
import zw.codinho.ridehail.driver.domain.DriverRepository;
import zw.codinho.ridehail.platform.PlatformAccountService;
import zw.codinho.ridehail.platform.domain.PlatformAccount;
import zw.codinho.ridehail.rider.domain.RiderRepository;
import zw.codinho.ridehail.ride.domain.RideRepository;
import zw.codinho.ridehail.ride.domain.RideStatus;
import zw.codinho.ridehail.shared.exception.BadRequestException;
import zw.codinho.ridehail.shared.exception.NotFoundException;
import zw.codinho.ridehail.wallet.WalletService;
import zw.codinho.ridehail.wallet.domain.WalletOwnerType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final String CURRENCY = "USD";

    private final DriverService driverService;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final RideRepository rideRepository;
    private final PlatformAccountService platformAccountService;
    private final AuthAccountRepository authAccountRepository;
    private final WalletService walletService;

    @Transactional
    public PlatformAccountResponse withdrawPlatformFunds(BigDecimal amount) {
        return toPlatformResponse(platformAccountService.withdraw(amount));
    }

    @Transactional
    public zw.codinho.ridehail.driver.rest.DriverResponse blockDriver(UUID driverId, String reason) {
        Driver driver = driverService.requireDriver(driverId);
        if (driver.getStatus() == zw.codinho.ridehail.driver.domain.DriverStatus.ON_TRIP) {
            throw new BadRequestException("A driver on an active trip cannot be blocked");
        }

        driver.setBlocked(true);
        driver.setBlockedReason(reason.trim());
        driver.setBlockedAt(OffsetDateTime.now());
        driver.setStatus(zw.codinho.ridehail.driver.domain.DriverStatus.OFFLINE);
        return driverService.toResponse(driverRepository.save(driver));
    }

    @Transactional
    public zw.codinho.ridehail.driver.rest.DriverResponse unblockDriver(UUID driverId) {
        Driver driver = driverService.requireDriver(driverId);
        driver.setBlocked(false);
        driver.setBlockedReason(null);
        driver.setBlockedAt(null);
        return driverService.toResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getDashboardMetrics() {
        PlatformAccount account = platformAccountService.getPrimaryAccount();
        BigDecimal totalRevenue = rideRepository.sumActualFareByStatus(RideStatus.COMPLETED).orElse(BigDecimal.ZERO);

        return new AdminDashboardMetricsResponse(
                riderRepository.count(),
                driverRepository.count(),
                driverRepository.countByBlockedTrue(),
                rideRepository.count(),
                rideRepository.countByStatus(RideStatus.COMPLETED),
                rideRepository.countByStatus(RideStatus.CANCELLED),
                totalRevenue,
                account.getAvailableBalance(),
                account.getTotalCommissionEarned(),
                account.getTotalWithdrawn(),
                CURRENCY);
    }

    @Transactional
    public AuthAccountRoleResponse updateUserRole(UUID authAccountId, UserRole role) {
        AuthAccount account = authAccountRepository.findById(authAccountId)
                .orElseThrow(() -> new NotFoundException("Auth account " + authAccountId + " was not found"));
        account.setRole(role);
        AuthAccount savedAccount = authAccountRepository.save(account);
        return new AuthAccountRoleResponse(savedAccount.getId(), savedAccount.getEmailAddress(), savedAccount.getRole());
    }

    public zw.codinho.ridehail.admin.rest.WalletBalanceResponse depositRiderFunds(UUID riderId, BigDecimal amount) {
        riderRepository.findById(riderId)
                .orElseThrow(() -> new NotFoundException("Rider " + riderId + " was not found"));
        return walletService.deposit(WalletOwnerType.RIDER, riderId, amount);
    }

    private PlatformAccountResponse toPlatformResponse(PlatformAccount account) {
        return new PlatformAccountResponse(
                account.getId(),
                account.getAvailableBalance(),
                account.getTotalCommissionEarned(),
                account.getTotalWithdrawn(),
                CURRENCY);
    }
}
