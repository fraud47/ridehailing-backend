package zw.codinho.ridehail.ride.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    List<Ride> findAllByRiderIdOrderByRequestedAtDesc(UUID riderId);

    List<Ride> findAllByDriverIdOrderByRequestedAtDesc(UUID driverId);

    long countByStatus(RideStatus status);

    @Query("select sum(r.actualFare) from Ride r where r.status = :status")
    Optional<BigDecimal> sumActualFareByStatus(RideStatus status);
}
