package zw.codinho.ridehail.driver.rest;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String registrationNumber,
        String make,
        String model,
        String color,
        Integer yearOfManufacture,
        Integer seatCapacity
) {
}
