package zw.codinho.ridehail.rider.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.codinho.ridehail.shared.domain.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "riders")
public class Rider extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String emailAddress;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(5.00);
}
