package com.fleet_management_backend.repository;

import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
}
