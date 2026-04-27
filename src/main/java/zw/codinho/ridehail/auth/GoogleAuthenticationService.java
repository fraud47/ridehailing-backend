package zw.codinho.ridehail.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.auth.domain.AuthAccount;
import zw.codinho.ridehail.auth.domain.AuthAccountRepository;
import zw.codinho.ridehail.auth.domain.AuthProvider;
import zw.codinho.ridehail.auth.domain.UserRole;
import zw.codinho.ridehail.auth.google.GoogleIdentityTokenVerifier;
import zw.codinho.ridehail.auth.google.GooglePrincipal;
import zw.codinho.ridehail.auth.jwt.LocalJwtService;
import zw.codinho.ridehail.driver.DriverService;
import zw.codinho.ridehail.driver.domain.Driver;
import zw.codinho.ridehail.driver.domain.DriverRepository;
import zw.codinho.ridehail.driver.rest.DriverResponse;
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.rider.domain.RiderRepository;
import zw.codinho.ridehail.rider.rest.RiderResponse;
import zw.codinho.ridehail.shared.exception.BadRequestException;
import zw.codinho.ridehail.shared.exception.ConflictException;
import zw.codinho.ridehail.wallet.WalletService;
import zw.codinho.ridehail.wallet.domain.WalletOwnerType;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

    private final GoogleIdentityTokenVerifier googleIdentityTokenVerifier;
    private final AuthAccountRepository authAccountRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final RiderRepository riderRepository;
    private final LocalJwtService localJwtService;
    private final WalletService walletService;

    @Transactional
    public AuthTokenResponse authenticate(GoogleLoginRequest request) {
        GooglePrincipal principal = googleIdentityTokenVerifier.verify(request.idToken());
        if (!principal.emailVerified()) {
            throw new BadRequestException("The Google account email address is not verified");
        }

        Optional<AuthAccount> existingAccount = authAccountRepository.findByProviderAndProviderSubject(AuthProvider.GOOGLE, principal.subject())
                .or(() -> authAccountRepository.findFirstByEmailAddressIgnoreCase(principal.emailAddress()))
                ;

        boolean newUser = existingAccount.isEmpty();
        AuthAccount account = existingAccount.orElseGet(AuthAccount::new);
        LinkedHashSet<UserRole> requestedRoles = resolveRequestedRoles(account, request.requestedRoles());

        Rider rider = null;
        if (requestedRoles.contains(UserRole.RIDER) || account.getRider() != null) {
            rider = resolveRider(account, principal, request.phoneNumber());
            requestedRoles.add(UserRole.RIDER);
        }

        Driver driver = null;
        if (requestedRoles.contains(UserRole.DRIVER) || account.getDriver() != null) {
            driver = resolveDriver(account, principal, request);
            requestedRoles.add(UserRole.DRIVER);
        }

        account.setProvider(AuthProvider.GOOGLE);
        account.setProviderSubject(principal.subject());
        account.setEmailAddress(principal.emailAddress().trim().toLowerCase());
        account.setDisplayName(principal.fullName());
        account.setRoles(normalizeRoles(requestedRoles, rider, driver));
        account.setRider(rider);
        account.setDriver(driver);

        AuthAccount savedAccount = authAccountRepository.save(account);
        if (rider != null) {
            walletService.ensureWallet(WalletOwnerType.RIDER, rider.getId());
        }
        if (driver != null) {
            walletService.ensureWallet(WalletOwnerType.DRIVER, driver.getId());
        }
        LocalJwtService.TokenResult tokenResult = localJwtService.issueToken(savedAccount);

        return new AuthTokenResponse(
                tokenResult.accessToken(),
                "Bearer",
                tokenResult.expiresAt(),
                newUser,
                savedAccount.getRoles().stream().sorted(Comparator.comparing(Enum::name)).toList(),
                rider == null ? null : toRiderResponse(rider),
                driver == null ? null : toDriverResponse(driver));
    }

    private LinkedHashSet<UserRole> resolveRequestedRoles(AuthAccount account, Set<UserRole> requestedRoles) {
        LinkedHashSet<UserRole> roles = new LinkedHashSet<>();
        if (requestedRoles != null) {
            requestedRoles.stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(roles::add);
        }
        if (roles.isEmpty()) {
            roles.addAll(account.getRoles());
        }
        if (roles.isEmpty()) {
            roles.add(UserRole.RIDER);
        }
        return roles;
    }

    private Rider resolveRider(AuthAccount account, GooglePrincipal principal, String phoneNumber) {
        if (account.getRider() != null) {
            Rider rider = account.getRider();
            rider.setFullName(principal.fullName());
            rider.setEmailAddress(principal.emailAddress().trim().toLowerCase());
            if (isProvided(phoneNumber)) {
                ensurePhoneNotUsed(phoneNumber, rider.getId());
                rider.setPhoneNumber(phoneNumber.trim());
            }
            return riderRepository.save(rider);
        }

        Optional<Rider> existingRider = riderRepository.findByEmailAddress(principal.emailAddress().trim().toLowerCase());
        if (existingRider.isPresent()) {
            Rider rider = existingRider.get();
            rider.setFullName(principal.fullName());
            if (isProvided(phoneNumber) && rider.getPhoneNumber() == null) {
                ensurePhoneNotUsed(phoneNumber, rider.getId());
                rider.setPhoneNumber(phoneNumber.trim());
            }
            return riderRepository.save(rider);
        }

        Rider rider = new Rider();
        rider.setFullName(principal.fullName());
        rider.setEmailAddress(principal.emailAddress().trim().toLowerCase());
        if (isProvided(phoneNumber)) {
            ensurePhoneNotUsed(phoneNumber, null);
            rider.setPhoneNumber(phoneNumber.trim());
        }
        return riderRepository.save(rider);
    }

    private Driver resolveDriver(AuthAccount account, GooglePrincipal principal, GoogleLoginRequest request) {
        GoogleDriverProfileRequest driverRequest = request.driver();

        if (account.getDriver() != null) {
            Driver driver = account.getDriver();
            driver.setFullName(principal.fullName());
            if (driverRequest != null) {
                applyDriverProfile(driver, driverRequest, request.phoneNumber(), driver.getId());
            }
            return driverRepository.save(driver);
        }

        if (driverRequest == null) {
            throw new BadRequestException("Driver profile details are required when requesting the DRIVER role");
        }

        Driver driver = new Driver();
        driver.setFullName(principal.fullName());
        applyDriverProfile(driver, driverRequest, request.phoneNumber(), null);
        return driverRepository.save(driver);
    }

    private void ensurePhoneNotUsed(String phoneNumber, java.util.UUID currentRiderId) {
        riderRepository.findByPhoneNumber(phoneNumber.trim())
                .filter(rider -> currentRiderId == null || !rider.getId().equals(currentRiderId))
                .ifPresent(rider -> {
                    throw new BadRequestException("Phone number is already associated with another rider");
                });
    }

    private void applyDriverProfile(Driver driver,
                                    GoogleDriverProfileRequest driverRequest,
                                    String fallbackPhoneNumber,
                                    java.util.UUID currentDriverId) {
        String phoneNumber = isProvided(driverRequest.phoneNumber()) ? driverRequest.phoneNumber().trim() : normalizeOptional(fallbackPhoneNumber);
        if (!isProvided(phoneNumber)) {
            throw new BadRequestException("Driver phone number is required when requesting the DRIVER role");
        }
        if (!isProvided(driverRequest.licenseNumber())) {
            throw new BadRequestException("Driver license number is required when requesting the DRIVER role");
        }
        if (driverRequest.currentLatitude() == null || driverRequest.currentLongitude() == null) {
            throw new BadRequestException("Driver location is required when requesting the DRIVER role");
        }

        ensureDriverPhoneNotUsed(phoneNumber, currentDriverId);
        ensureDriverLicenseNotUsed(driverRequest.licenseNumber().trim(), currentDriverId);

        driver.setPhoneNumber(phoneNumber);
        driver.setLicenseNumber(driverRequest.licenseNumber().trim());
        driver.setCurrentLatitude(driverRequest.currentLatitude());
        driver.setCurrentLongitude(driverRequest.currentLongitude());
    }

    private void ensureDriverPhoneNotUsed(String phoneNumber, java.util.UUID currentDriverId) {
        driverRepository.findByPhoneNumber(phoneNumber.trim())
                .filter(driver -> currentDriverId == null || !driver.getId().equals(currentDriverId))
                .ifPresent(driver -> {
                    throw new ConflictException("A driver already exists with phone number " + phoneNumber);
                });
    }

    private void ensureDriverLicenseNotUsed(String licenseNumber, java.util.UUID currentDriverId) {
        driverRepository.findByLicenseNumber(licenseNumber.trim())
                .filter(driver -> currentDriverId == null || !driver.getId().equals(currentDriverId))
                .ifPresent(driver -> {
                    throw new ConflictException("A driver already exists with license number " + licenseNumber);
                });
    }

    private LinkedHashSet<UserRole> normalizeRoles(Set<UserRole> roles, Rider rider, Driver driver) {
        LinkedHashSet<UserRole> normalizedRoles = new LinkedHashSet<>(roles);
        if (rider != null) {
            normalizedRoles.add(UserRole.RIDER);
        }
        if (driver != null) {
            normalizedRoles.add(UserRole.DRIVER);
        }
        return normalizedRoles;
    }

    private boolean isProvided(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.trim().isBlank();
    }

    private String normalizeOptional(String value) {
        return isProvided(value) ? value.trim() : null;
    }

    private RiderResponse toRiderResponse(Rider rider) {
        var wallet = walletService.getWallet(WalletOwnerType.RIDER, rider.getId());
        return new RiderResponse(
                rider.getId(),
                rider.getFullName(),
                rider.getPhoneNumber(),
                rider.getEmailAddress(),
                rider.getRating(),
                wallet.balance(),
                rider.getCreatedAt());
    }

    private DriverResponse toDriverResponse(Driver driver) {
        return driverService.toResponse(driver);
    }
}
