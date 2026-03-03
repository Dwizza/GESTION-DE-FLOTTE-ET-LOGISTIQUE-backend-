package com.fleet_management_backend.entity;

import com.fleet_management_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String profileImageUrl;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Driver driver;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Client client;

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
