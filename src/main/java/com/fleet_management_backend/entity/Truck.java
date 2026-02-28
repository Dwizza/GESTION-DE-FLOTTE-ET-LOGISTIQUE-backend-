package com.fleet_management_backend.entity;

import com.fleet_management_backend.entity.enums.TruckStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckStatus status;

    private BigDecimal totalMileage;

    @OneToMany(mappedBy = "truck")
    private List<CarburantTransaction> carburantTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "truck")
    private List<TrackingPoint> trackingPoints = new ArrayList<>();

    @OneToMany(mappedBy = "truck")
    private List<Maintenance> maintenances = new ArrayList<>();

    @OneToMany(mappedBy = "truck")
    private List<TripTruck> tripTrucks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
