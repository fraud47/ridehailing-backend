package zw.codinho.ridehail.platform.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlatformAccountRepository extends JpaRepository<PlatformAccount, UUID> {

    Optional<PlatformAccount> findByAccountKey(String accountKey);
}
