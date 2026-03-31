package zw.codinho.ridehail.platform.domain;

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
@Table(name = "platform_accounts")
public class PlatformAccount extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String accountKey;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionEarned = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;
}
