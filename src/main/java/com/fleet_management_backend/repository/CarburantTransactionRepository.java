package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.CarburantTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarburantTransactionRepository extends JpaRepository<CarburantTransaction, UUID> {
    @Query("SELECT SUM(c.cout) FROM CarburantTransaction c")
    java.math.BigDecimal sumTotalFuelCost();
}
