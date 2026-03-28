package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.RegisterManagerRequest;
import com.fleet_management_backend.dto.response.RegisterManagerResponse;
import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;
import com.fleet_management_backend.exception.BadRequestException;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.UserMapper;
import com.fleet_management_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.request.UpdateClientRequest;
import com.fleet_management_backend.dto.response.RegisterClientResponse;
import com.fleet_management_backend.dto.response.ClientResponse;
import com.fleet_management_backend.entity.Client;
import com.fleet_management_backend.mapper.ClientMapper;
import com.fleet_management_backend.repository.ClientRepository;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import com.fleet_management_backend.dto.response.ManagerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@AllArgsConstructor
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

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
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new BadRequestException("User is not a logistics manager");
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
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new BadRequestException("User is not a logistics manager");
        }

        return userMapper.toManagerResponse(manager);
    }

    @Transactional
    public com.fleet_management_backend.dto.response.ManagerResponse updateManager(UUID managerId,
            com.fleet_management_backend.dto.request.UpdateManagerRequest request) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != Role.LOGISTICS_MANAGER) {
            throw new BadRequestException("User is not a logistics manager");
        }

        manager.setFirstName(request.getFirstName());
        manager.setLastName(request.getLastName());
        if (request.getActive() != null) {
            manager.setActive(request.getActive());
        }

        manager = userRepository.save(manager);
        return userMapper.toManagerResponse(manager);
    }

    @Transactional
    public RegisterClientResponse createClient(RegisterClientRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = userMapper.toEntity(req);
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user = userRepository.save(user);

        Client client = clientMapper.toEntity(req);
        client.setUser(user);
        client = clientRepository.save(client);

        user.setClient(client);
        return clientMapper.toRegisterClientResponse(user, client);
    }

    @Transactional
    public ClientResponse updateClient(UUID clientId, UpdateClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        client.setCompanyName(request.getCompanyName());
        client.setAddress(request.getAddress());
        client.setPhone(request.getPhone());

        client = clientRepository.save(client);

        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .address(client.getAddress())
                .phone(client.getPhone())
                .email(client.getUser().getEmail())
                .role(client.getUser().getRole().name())
                .build();
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // Ensure to delete trips or throw explicit conflict if related rows exist
        if (!client.getTrips().isEmpty()) {
            throw new ConflictException("Cannot delete client with existing trips");
        }

        User user = client.getUser();
        clientRepository.delete(client);
        userRepository.delete(user);
    }

    public PaginatedResponse<ManagerResponse> getPaginatedManagers(Pageable pageable) {
        Page<User> managersPage = userRepository.findByRole(Role.LOGISTICS_MANAGER, pageable);
        return PaginatedResponse.<ManagerResponse>builder()
                .content(managersPage.getContent().stream().map(userMapper::toManagerResponse).toList())
                .pageNumber(managersPage.getNumber())
                .pageSize(managersPage.getSize())
                .totalElements(managersPage.getTotalElements())
                .totalPages(managersPage.getTotalPages())
                .last(managersPage.isLast())
                .build();
    }

    public PaginatedResponse<ClientResponse> getPaginatedClients(Pageable pageable) {
        Page<Client> clientsPage = clientRepository.findAll(pageable);
        return PaginatedResponse.<ClientResponse>builder()
                .content(clientsPage.getContent().stream()
                        .map(c -> ClientResponse.builder()
                                .id(c.getId())
                                .companyName(c.getCompanyName())
                                .address(c.getAddress())
                                .phone(c.getPhone())
                                .email(c.getUser().getEmail())
                                .role(c.getUser().getRole().name())
                                .build())
                        .toList())
                .pageNumber(clientsPage.getNumber())
                .pageSize(clientsPage.getSize())
                .totalElements(clientsPage.getTotalElements())
                .totalPages(clientsPage.getTotalPages())
                .last(clientsPage.isLast())
                .build();
    }
}
