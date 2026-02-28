package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TruckRepository extends JpaRepository<Truck, UUID> {
    boolean existsByRegistrationNumber(String registrationNumber);
}
