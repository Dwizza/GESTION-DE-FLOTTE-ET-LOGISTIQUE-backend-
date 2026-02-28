package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.RegisterManagerRequest;
import com.fleet_management_backend.dto.response.RegisterManagerResponse;
import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.mapper.UserMapper;
import com.fleet_management_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public RegisterManagerResponse CreateManager(RegisterManagerRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email is already in use");
        }

        User manager = userMapper.toEntity(request);
        manager.setRole(Role.LOGISTICS_MANAGER);
        manager.setActive(true);
        manager.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedManager = userRepository.save(manager);

        return userMapper.toRegisterManagerDto(savedManager);
    }

    public void DeleteManager(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new RuntimeException("User is not a logistics manager");
        }

        userRepository.delete(manager);
    }

    public java.util.List<com.fleet_management_backend.dto.response.ManagerResponse> getAllManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.LOGISTICS_MANAGER)
                .map(userMapper::toManagerResponse)
                .toList();
    }

    public com.fleet_management_backend.dto.response.ManagerResponse getManagerById(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new RuntimeException("User is not a logistics manager");
        }

        return userMapper.toManagerResponse(manager);
    }

    @Transactional
    public com.fleet_management_backend.dto.response.ManagerResponse updateManager(UUID managerId,
            com.fleet_management_backend.dto.request.UpdateManagerRequest request) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new RuntimeException("User is not a logistics manager");
        }

        manager.setFirstName(request.getFirstName());
        manager.setLastName(request.getLastName());
        if (request.getActive() != null) {
            manager.setActive(request.getActive());
        }

        manager = userRepository.save(manager);
        return userMapper.toManagerResponse(manager);
    }

}
