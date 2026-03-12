package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.TrackingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackingPointRepository extends JpaRepository<TrackingPoint, UUID> {
    Optional<TrackingPoint> findTopByTruckIdOrderByTimestampDesc(UUID truckId);
    List<TrackingPoint> findByTripIdOrderByTimestampAsc(UUID tripId);

    @org.springframework.data.jpa.repository.Query("SELECT t1 FROM TrackingPoint t1 WHERE t1.timestamp = (SELECT MAX(t2.timestamp) FROM TrackingPoint t2 WHERE t2.truck.id = t1.truck.id)")
    List<TrackingPoint> findAllLatestPoints();
}
