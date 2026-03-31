package zw.codinho.ridehail.driver.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByPhoneNumber(String phoneNumber);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    List<Driver> findAllByBlockedFalseAndStatus(DriverStatus status);

    long countByBlockedTrue();
}
