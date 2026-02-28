package com.fleet_management_backend.entity;

import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.entity.enums.TrailerType;
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
@Table(name = "trailers")
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrailerType type;

    private BigDecimal maxWeight;

    private BigDecimal maxVolume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrailerStatus status;

    @OneToMany(mappedBy = "trailer")
    private List<Maintenance> maintenances = new ArrayList<>();

    @OneToMany(mappedBy = "trailer")
    private List<TripTrailer> tripTrailers = new ArrayList<>();

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
