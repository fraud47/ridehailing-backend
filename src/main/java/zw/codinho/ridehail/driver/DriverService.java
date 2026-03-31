package zw.codinho.ridehail.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.driver.domain.Driver;
import zw.codinho.ridehail.driver.domain.DriverRepository;
import zw.codinho.ridehail.driver.domain.DriverStatus;
import zw.codinho.ridehail.driver.domain.Vehicle;
import zw.codinho.ridehail.driver.rest.AssignVehicleRequest;
import zw.codinho.ridehail.driver.rest.CreateDriverRequest;
import zw.codinho.ridehail.driver.rest.DriverResponse;
import zw.codinho.ridehail.driver.rest.UpdateDriverAvailabilityRequest;
import zw.codinho.ridehail.driver.rest.VehicleResponse;
import zw.codinho.ridehail.shared.exception.BadRequestException;
import zw.codinho.ridehail.shared.exception.ConflictException;
import zw.codinho.ridehail.shared.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request) {
        driverRepository.findByPhoneNumber(request.phoneNumber())
                .ifPresent(driver -> {
                    throw new ConflictException("A driver already exists with phone number " + request.phoneNumber());
                });

        driverRepository.findByLicenseNumber(request.licenseNumber())
                .ifPresent(driver -> {
                    throw new ConflictException("A driver already exists with license number " + request.licenseNumber());
                });

        Driver driver = new Driver();
        driver.setFullName(request.fullName());
        driver.setPhoneNumber(request.phoneNumber());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setCurrentLatitude(request.currentLatitude());
        driver.setCurrentLongitude(request.currentLongitude());

        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse assignVehicle(UUID driverId, AssignVehicleRequest request) {
        Driver driver = requireDriver(driverId);
        Vehicle vehicle = driver.getVehicle();
        if (vehicle == null) {
            vehicle = new Vehicle();
        }
        vehicle.setRegistrationNumber(request.registrationNumber());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setColor(request.color());
        vehicle.setYearOfManufacture(request.yearOfManufacture());
        vehicle.setSeatCapacity(request.seatCapacity());
        driver.setVehicle(vehicle);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse updateAvailability(UUID driverId, UpdateDriverAvailabilityRequest request) {
        Driver driver = requireDriver(driverId);

        if (request.available() && driver.getVehicle() == null) {
            throw new BadRequestException("A driver cannot go available without a registered vehicle");
        }

        if (driver.getStatus() == DriverStatus.ON_TRIP && !request.available()) {
            throw new BadRequestException("A driver on an active ride cannot be marked offline");
        }

        driver.setCurrentLatitude(request.currentLatitude());
        driver.setCurrentLongitude(request.currentLongitude());
        driver.setStatus(Boolean.TRUE.equals(request.available()) ? DriverStatus.AVAILABLE : DriverStatus.OFFLINE);

        return toResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID driverId) {
        return toResponse(requireDriver(driverId));
    }

    public Driver requireDriver(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Driver " + driverId + " was not found"));
    }

    @Transactional
    public void markDriverOnTrip(Driver driver) {
        driver.setStatus(DriverStatus.ON_TRIP);
        driverRepository.save(driver);
    }

    @Transactional
    public void markDriverAvailable(Driver driver) {
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);
    }

    private DriverResponse toResponse(Driver driver) {
        VehicleResponse vehicleResponse = null;
        if (driver.getVehicle() != null) {
            vehicleResponse = new VehicleResponse(
                    driver.getVehicle().getId(),
                    driver.getVehicle().getRegistrationNumber(),
                    driver.getVehicle().getMake(),
                    driver.getVehicle().getModel(),
                    driver.getVehicle().getColor(),
                    driver.getVehicle().getYearOfManufacture(),
                    driver.getVehicle().getSeatCapacity());
        }

        return new DriverResponse(
                driver.getId(),
                driver.getFullName(),
                driver.getPhoneNumber(),
                driver.getLicenseNumber(),
                driver.getStatus(),
                driver.getRating(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                vehicleResponse,
                driver.getCreatedAt());
    }
}
