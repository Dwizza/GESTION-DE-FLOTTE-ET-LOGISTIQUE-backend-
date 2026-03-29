package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrailerRepository extends JpaRepository<Trailer, UUID> {
    @Query("SELECT t.type, COUNT(t) FROM Trailer t GROUP BY t.type")
    List<Object[]> countTrailersByType();
}
