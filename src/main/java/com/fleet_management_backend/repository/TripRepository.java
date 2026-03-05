package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fleet_management_backend.entity.enums.TripStatus;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    boolean existsByReference(String reference);

    @Query("SELECT t FROM Trip t JOIN t.tripTrucks tt WHERE tt.truck.id = :truckId AND t.status = :status")
    List<Trip> findTripsByTruckIdAndStatus(@Param("truckId") UUID truckId, @Param("status") TripStatus status);

    List<Trip> findByDriverIdOrderByStartDateDesc(UUID driverId);
}
