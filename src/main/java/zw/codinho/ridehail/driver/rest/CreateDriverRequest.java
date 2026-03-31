package zw.codinho.ridehail.driver.rest;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateDriverRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Phone number is required")
        String phoneNumber,
        @NotBlank(message = "License number is required")
        String licenseNumber,
        @NotNull(message = "Current latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal currentLatitude,
        @NotNull(message = "Current longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal currentLongitude
) {
}
