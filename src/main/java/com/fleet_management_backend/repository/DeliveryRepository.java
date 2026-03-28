package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    boolean existsByReference(String reference);

    java.util.List<Delivery> findByTripId(UUID tripId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.prix) FROM Delivery d")
    java.math.BigDecimal sumTotalRevenue();
}
