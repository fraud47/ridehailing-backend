package zw.codinho.ridehail.ride.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    List<Ride> findAllByRiderIdOrderByRequestedAtDesc(UUID riderId);

    List<Ride> findAllByDriverIdOrderByRequestedAtDesc(UUID driverId);
}
