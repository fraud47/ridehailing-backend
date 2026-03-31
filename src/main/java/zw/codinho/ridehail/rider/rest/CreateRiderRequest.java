package zw.codinho.ridehail.rider.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateRiderRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Phone number is required")
        String phoneNumber,
        @Email(message = "A valid email address is required")
        @NotBlank(message = "Email address is required")
        String emailAddress
) {
}
