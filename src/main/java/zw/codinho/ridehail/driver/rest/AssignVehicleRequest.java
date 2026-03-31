package zw.codinho.ridehail.driver.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignVehicleRequest(
        @NotBlank(message = "Registration number is required")
        String registrationNumber,
        @NotBlank(message = "Vehicle make is required")
        String make,
        @NotBlank(message = "Vehicle model is required")
        String model,
        @NotBlank(message = "Vehicle color is required")
        String color,
        @NotNull(message = "Vehicle year is required")
        @Min(value = 2000, message = "Vehicle year must be realistic")
        @Max(value = 2100, message = "Vehicle year must be realistic")
        Integer yearOfManufacture,
        @NotNull(message = "Seat capacity is required")
        @Min(value = 1, message = "Seat capacity must be positive")
        Integer seatCapacity
) {
}
