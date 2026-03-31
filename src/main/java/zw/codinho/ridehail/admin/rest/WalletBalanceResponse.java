package zw.codinho.ridehail.admin.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletBalanceResponse(
        UUID ownerId,
        String ownerType,
        BigDecimal balance,
        String currency
) {
}
