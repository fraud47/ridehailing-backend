package zw.codinho.ridehail.ride.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.codinho.ridehail.driver.domain.Driver;
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.shared.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "rides")
public class Ride extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String dropoffAddress;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLongitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLongitude;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceInKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quotedFare;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualFare;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    private OffsetDateTime assignedAt;

    private OffsetDateTime pickedUpAt;

    private OffsetDateTime completedAt;
}
