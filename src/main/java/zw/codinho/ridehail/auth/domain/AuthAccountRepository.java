package zw.codinho.ridehail.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, UUID> {

    Optional<AuthAccount> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    Optional<AuthAccount> findFirstByEmailAddressIgnoreCase(String emailAddress);
}
