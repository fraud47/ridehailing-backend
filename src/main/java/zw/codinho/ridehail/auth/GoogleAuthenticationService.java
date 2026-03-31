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
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.rider.domain.RiderRepository;
import zw.codinho.ridehail.rider.rest.RiderResponse;
import zw.codinho.ridehail.shared.exception.BadRequestException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

    private final GoogleIdentityTokenVerifier googleIdentityTokenVerifier;
    private final AuthAccountRepository authAccountRepository;
    private final RiderRepository riderRepository;
    private final LocalJwtService localJwtService;

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

        Rider rider = resolveRider(account, principal, request.phoneNumber());

        account.setProvider(AuthProvider.GOOGLE);
        account.setProviderSubject(principal.subject());
        account.setEmailAddress(principal.emailAddress().trim().toLowerCase());
        account.setDisplayName(principal.fullName());
        account.setRole(UserRole.RIDER);
        account.setRider(rider);

        AuthAccount savedAccount = authAccountRepository.save(account);
        LocalJwtService.TokenResult tokenResult = localJwtService.issueToken(savedAccount);

        return new AuthTokenResponse(
                tokenResult.accessToken(),
                "Bearer",
                tokenResult.expiresAt(),
                newUser,
                toRiderResponse(rider));
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

    private void ensurePhoneNotUsed(String phoneNumber, java.util.UUID currentRiderId) {
        riderRepository.findByPhoneNumber(phoneNumber.trim())
                .filter(rider -> currentRiderId == null || !rider.getId().equals(currentRiderId))
                .ifPresent(rider -> {
                    throw new BadRequestException("Phone number is already associated with another rider");
                });
    }

    private boolean isProvided(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.trim().isBlank();
    }

    private RiderResponse toRiderResponse(Rider rider) {
        return new RiderResponse(
                rider.getId(),
                rider.getFullName(),
                rider.getPhoneNumber(),
                rider.getEmailAddress(),
                rider.getRating(),
                rider.getCreatedAt());
    }
}
