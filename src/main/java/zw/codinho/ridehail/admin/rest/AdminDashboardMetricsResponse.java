package zw.codinho.ridehail.admin.rest;

import java.math.BigDecimal;

public record AdminDashboardMetricsResponse(
        long totalRiders,
        long totalDrivers,
        long blockedDrivers,
        long totalRides,
        long completedRides,
        long cancelledRides,
        BigDecimal totalRideRevenue,
        BigDecimal platformAvailableBalance,
        BigDecimal totalPlatformCommissionEarned,
        BigDecimal totalPlatformWithdrawn,
        String currency
) {
}
