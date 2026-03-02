package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.DeliveryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryCategoryRepository extends JpaRepository<DeliveryCategory, UUID> {
    boolean existsByName(String name);
}
