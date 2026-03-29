package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    boolean existsByReference(String reference);

    List<Delivery> findByTripId(UUID tripId);

    @Query("SELECT SUM(d.prix) FROM Delivery d")
    BigDecimal sumTotalRevenue();
}
