package zw.codinho.ridehail.ride.rest;

import java.math.BigDecimal;

public record RideQuoteResponse(
        BigDecimal distanceInKm,
        BigDecimal estimatedFare,
        String currency,
        String pricingModel
) {
}
