package zw.codinho.ridehail.admin.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record PlatformAccountResponse(
        UUID id,
        BigDecimal availableBalance,
        BigDecimal totalCommissionEarned,
        BigDecimal totalWithdrawn,
        String currency
) {
}
