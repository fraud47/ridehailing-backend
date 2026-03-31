package zw.codinho.ridehail.admin.rest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositFundsRequest(
        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
        BigDecimal amount
) {
}
