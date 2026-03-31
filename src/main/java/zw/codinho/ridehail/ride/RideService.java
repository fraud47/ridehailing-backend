package zw.codinho.ridehail.ride;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.driver.DriverService;
import zw.codinho.ridehail.driver.domain.Driver;
import zw.codinho.ridehail.platform.PlatformAccountService;
import zw.codinho.ridehail.rider.RiderService;
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.ride.domain.Ride;
import zw.codinho.ridehail.ride.domain.RideRepository;
import zw.codinho.ridehail.ride.domain.RideStatus;
import zw.codinho.ridehail.ride.rest.CreateRideRequest;
import zw.codinho.ridehail.ride.rest.RideQuoteRequest;
import zw.codinho.ridehail.ride.rest.RideQuoteResponse;
import zw.codinho.ridehail.ride.rest.RideResponse;
import zw.codinho.ridehail.shared.exception.BadRequestException;
import zw.codinho.ridehail.shared.exception.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideService {

    private static final BigDecimal BASE_FARE = new BigDecimal("2.50");
    private static final BigDecimal RATE_PER_KM = new BigDecimal("1.35");
    private static final BigDecimal SHORT_TRIP_SURGE = new BigDecimal("1.10");
    private static final BigDecimal PLATFORM_COMMISSION = new BigDecimal("0.20");
    private static final String PRICING_MODEL = "base_plus_distance";
    private static final String CURRENCY = "USD";

    private final RideRepository rideRepository;
    private final RiderService riderService;
    private final DriverService driverService;
    private final PlatformAccountService platformAccountService;

    @Transactional(readOnly = true)
    public RideQuoteResponse quoteRide(RideQuoteRequest request) {
        BigDecimal distanceInKm = calculateDistanceKm(
                request.pickupLatitude(),
                request.pickupLongitude(),
                request.dropoffLatitude(),
                request.dropoffLongitude());

        return new RideQuoteResponse(distanceInKm, calculateFare(distanceInKm), CURRENCY, PRICING_MODEL);
    }

    @Transactional
    public RideResponse createRide(CreateRideRequest request) {
        Rider rider = riderService.requireRider(request.riderId());
        BigDecimal distanceInKm = calculateDistanceKm(
                request.pickupLatitude(),
                request.pickupLongitude(),
                request.dropoffLatitude(),
                request.dropoffLongitude());

        Driver assignedDriver = findNearestAvailableDriver(request.pickupLatitude(), request.pickupLongitude());
        driverService.markDriverOnTrip(assignedDriver);

        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setDriver(assignedDriver);
        ride.setStatus(RideStatus.DRIVER_ASSIGNED);
        ride.setPickupAddress(request.pickupAddress());
        ride.setDropoffAddress(request.dropoffAddress());
        ride.setPickupLatitude(request.pickupLatitude());
        ride.setPickupLongitude(request.pickupLongitude());
        ride.setDropoffLatitude(request.dropoffLatitude());
        ride.setDropoffLongitude(request.dropoffLongitude());
        ride.setDistanceInKm(distanceInKm);
        ride.setQuotedFare(calculateFare(distanceInKm));
        ride.setPlatformCommission(PLATFORM_COMMISSION);
        ride.setRequestedAt(OffsetDateTime.now());
        ride.setAssignedAt(OffsetDateTime.now());

        return toResponse(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse updateRideStatus(UUID rideId, RideStatus nextStatus) {
        Ride ride = requireRideEntity(rideId);
        validateTransition(ride.getStatus(), nextStatus);

        ride.setStatus(nextStatus);
        if (nextStatus == RideStatus.IN_PROGRESS) {
            ride.setPickedUpAt(OffsetDateTime.now());
        }
        if (nextStatus == RideStatus.COMPLETED) {
            ride.setCompletedAt(OffsetDateTime.now());
            ride.setActualFare(ride.getQuotedFare());
            riderService.debitWallet(ride.getRider().getId(), ride.getActualFare());
            if (ride.getDriver() != null) {
                driverService.creditRideEarnings(ride.getDriver().getId(), ride.getActualFare(), ride.getPlatformCommission());
                driverService.markDriverAvailable(ride.getDriver());
            }
            platformAccountService.captureCommission(ride.getPlatformCommission());
        }
        if (nextStatus == RideStatus.CANCELLED) {
            ride.setCompletedAt(OffsetDateTime.now());
            if (ride.getDriver() != null) {
                driverService.markDriverAvailable(ride.getDriver());
            }
        }

        return toResponse(rideRepository.save(ride));
    }

    @Transactional(readOnly = true)
    public RideResponse getRide(UUID rideId) {
        return toResponse(requireRideEntity(rideId));
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getRides(UUID riderId, UUID driverId) {
        List<Ride> rides;
        if (riderId != null) {
            rides = rideRepository.findAllByRiderIdOrderByRequestedAtDesc(riderId);
        } else if (driverId != null) {
            rides = rideRepository.findAllByDriverIdOrderByRequestedAtDesc(driverId);
        } else {
            rides = rideRepository.findAll();
        }

        return rides.stream()
                .map(this::toResponse)
                .toList();
    }

    private Driver findNearestAvailableDriver(BigDecimal pickupLatitude, BigDecimal pickupLongitude) {
        return driverService.getAvailableDrivers().stream()
                .min((left, right) -> distanceToPickup(left.getCurrentLatitude(), left.getCurrentLongitude(), pickupLatitude, pickupLongitude)
                        .compareTo(distanceToPickup(right.getCurrentLatitude(), right.getCurrentLongitude(), pickupLatitude, pickupLongitude)))
                .orElseThrow(() -> new NotFoundException("No available driver could be assigned for this ride"));
    }

    private BigDecimal distanceToPickup(BigDecimal driverLatitude,
                                        BigDecimal driverLongitude,
                                        BigDecimal pickupLatitude,
                                        BigDecimal pickupLongitude) {
        return calculateDistanceKm(driverLatitude, driverLongitude, pickupLatitude, pickupLongitude);
    }

    private void validateTransition(RideStatus currentStatus, RideStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        EnumSet<RideStatus> allowedTransitions = switch (currentStatus) {
            case REQUESTED -> EnumSet.of(RideStatus.DRIVER_ASSIGNED, RideStatus.CANCELLED);
            case DRIVER_ASSIGNED -> EnumSet.of(RideStatus.DRIVER_EN_ROUTE, RideStatus.CANCELLED);
            case DRIVER_EN_ROUTE -> EnumSet.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED);
            case IN_PROGRESS -> EnumSet.of(RideStatus.COMPLETED, RideStatus.CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(RideStatus.class);
        };

        if (!allowedTransitions.contains(nextStatus)) {
            throw new BadRequestException("Cannot move ride from " + currentStatus + " to " + nextStatus);
        }
    }

    public Ride requireRideEntity(UUID rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Ride " + rideId + " was not found"));
    }

    private BigDecimal calculateFare(BigDecimal distanceInKm) {
        BigDecimal fare = BASE_FARE.add(distanceInKm.multiply(RATE_PER_KM));
        if (distanceInKm.compareTo(new BigDecimal("5.00")) < 0) {
            fare = fare.multiply(SHORT_TRIP_SURGE);
        }
        return fare.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistanceKm(BigDecimal startLatitude,
                                           BigDecimal startLongitude,
                                           BigDecimal endLatitude,
                                           BigDecimal endLongitude) {
        double earthRadius = 6371.0d;
        double startLat = Math.toRadians(startLatitude.doubleValue());
        double endLat = Math.toRadians(endLatitude.doubleValue());
        double latDiff = Math.toRadians(endLatitude.doubleValue() - startLatitude.doubleValue());
        double lonDiff = Math.toRadians(endLongitude.doubleValue() - startLongitude.doubleValue());

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadius * c).setScale(2, RoundingMode.HALF_UP);
    }

    private RideResponse toResponse(Ride ride) {
        UUID driverId = ride.getDriver() != null ? ride.getDriver().getId() : null;
        String driverName = ride.getDriver() != null ? ride.getDriver().getFullName() : null;

        return new RideResponse(
                ride.getId(),
                ride.getRider().getId(),
                ride.getRider().getFullName(),
                driverId,
                driverName,
                ride.getStatus(),
                ride.getPickupAddress(),
                ride.getDropoffAddress(),
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getDropoffLatitude(),
                ride.getDropoffLongitude(),
                ride.getDistanceInKm(),
                ride.getQuotedFare(),
                ride.getActualFare(),
                ride.getPlatformCommission(),
                ride.getRequestedAt(),
                ride.getAssignedAt(),
                ride.getPickedUpAt(),
                ride.getCompletedAt());
    }
}
