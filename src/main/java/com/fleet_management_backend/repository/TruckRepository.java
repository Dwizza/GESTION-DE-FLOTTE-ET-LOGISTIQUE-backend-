package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TruckRepository extends JpaRepository<Truck, UUID> {
    boolean existsByRegistrationNumber(String registrationNumber);

    @org.springframework.data.jpa.repository.Query("SELECT t.status, COUNT(t) FROM Truck t GROUP BY t.status")
    java.util.List<Object[]> countTrucksByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(t.brand, 'Autre'), COUNT(t) FROM Truck t GROUP BY t.brand")
    java.util.List<Object[]> countTrucksByBrand();
}
