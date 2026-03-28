package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);

    long countByAvailableTrue();
}
