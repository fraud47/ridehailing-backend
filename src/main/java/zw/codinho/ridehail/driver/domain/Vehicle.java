package zw.codinho.ridehail.driver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.codinho.ridehail.shared.domain.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer yearOfManufacture;

    @Column(nullable = false)
    private Integer seatCapacity;
}
