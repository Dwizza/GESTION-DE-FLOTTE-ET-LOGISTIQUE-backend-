package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.TripTruck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripTruckRepository extends JpaRepository<TripTruck, UUID> {
}
