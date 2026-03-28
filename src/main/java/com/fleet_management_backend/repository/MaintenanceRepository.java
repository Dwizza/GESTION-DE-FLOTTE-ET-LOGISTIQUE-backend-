package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    boolean existsByReference(String reference);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(m.cout) FROM Maintenance m")
    java.math.BigDecimal sumTotalMaintenanceCost();
}
