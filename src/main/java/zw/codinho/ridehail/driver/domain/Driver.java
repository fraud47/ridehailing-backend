package zw.codinho.ridehail.driver.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.codinho.ridehail.shared.domain.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.OFFLINE;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(5.00);

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean blocked;

    private String blockedReason;

    private java.time.OffsetDateTime blockedAt;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
}
