package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.request.UpdateClientRequest;
import com.fleet_management_backend.dto.response.RegisterClientResponse;
import com.fleet_management_backend.dto.request.RegisterManagerRequest;
import com.fleet_management_backend.dto.response.ClientResponse;
import com.fleet_management_backend.dto.response.RegisterDriverResponse;
import com.fleet_management_backend.dto.response.RegisterManagerResponse;
import com.fleet_management_backend.repository.ClientRepository;
import com.fleet_management_backend.service.AdminService;
import com.fleet_management_backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import com.fleet_management_backend.dto.response.ManagerResponse;
import com.fleet_management_backend.dto.response.DriverResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.UUID;

@RequestMapping("/api/admin")
@RestController
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DriverService driverService;
    private final com.fleet_management_backend.service.TripService tripService;
    private final ClientRepository clientRepository;

    @PostMapping("/create/manager")
    public ResponseEntity<RegisterManagerResponse> createManager(@Valid @RequestBody RegisterManagerRequest request) {
        RegisterManagerResponse manager = adminService.createManager(request);
        return ResponseEntity.ok(manager);
    }

    @DeleteMapping("/delete/manager/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable UUID id) {
        adminService.deleteManager(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/driver")
    public ResponseEntity<RegisterDriverResponse> createDriver(@Valid @RequestBody RegisterDriverRequest request) {
        RegisterDriverResponse driver = driverService.createDriver(request);
        return ResponseEntity.ok(driver);
    }

    @DeleteMapping("/delete/driver/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/managers")
    public ResponseEntity<java.util.List<com.fleet_management_backend.dto.response.ManagerResponse>> getAllManagers() {
        return ResponseEntity.ok(adminService.getAllManagers());
    }

    @GetMapping("/managers/page")
    public ResponseEntity<PaginatedResponse<ManagerResponse>> getPaginatedManagers(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.getPaginatedManagers(pageable));
    }

    @GetMapping("/manager/{id}")
    public ResponseEntity<com.fleet_management_backend.dto.response.ManagerResponse> getManagerById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getManagerById(id));
    }

    @PutMapping("/update/manager/{id}")
    public ResponseEntity<com.fleet_management_backend.dto.response.ManagerResponse> updateManager(
            @PathVariable UUID id,
            @Valid @RequestBody com.fleet_management_backend.dto.request.UpdateManagerRequest request) {
        return ResponseEntity.ok(adminService.updateManager(id, request));
    }

    @GetMapping("/drivers")
    public ResponseEntity<java.util.List<com.fleet_management_backend.dto.response.DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/drivers/page")
    public ResponseEntity<PaginatedResponse<DriverResponse>> getPaginatedDrivers(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(driverService.getPaginatedDrivers(pageable));
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<com.fleet_management_backend.dto.response.DriverResponse> getDriverById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @PutMapping("/update/driver/{id}")
    public ResponseEntity<com.fleet_management_backend.dto.response.DriverResponse> updateDriver(@PathVariable UUID id,
            @Valid @RequestBody com.fleet_management_backend.dto.request.UpdateDriverRequest request) {
        return ResponseEntity.ok(driverService.updateDriver(id, request));
    }

    @GetMapping("/driver/{id}/trips")
    public ResponseEntity<java.util.List<com.fleet_management_backend.dto.response.TripResponse>> getDriverTrips(
            @PathVariable UUID id) {
        return ResponseEntity.ok(tripService.getTripsByDriverId(id));
    }

    @GetMapping("/driver/{id}/trips/page")
    public ResponseEntity<PaginatedResponse<com.fleet_management_backend.dto.response.TripResponse>> getPaginatedDriverTrips(
            @PathVariable UUID id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tripService.getPaginatedTripsByDriverId(id, pageable));
    }

    @GetMapping("/clients")
    @Transactional(readOnly = true)
    public ResponseEntity<java.util.List<ClientResponse>> getAllClients() {
        var clients = clientRepository.findAll().stream()
                .map(c -> ClientResponse.builder()
                        .id(c.getId())
                        .companyName(c.getCompanyName())
                        .address(c.getAddress())
                        .phone(c.getPhone())
                        .email(c.getUser().getEmail())
                        .role(c.getUser().getRole().name())
                        .build())
                .toList();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/page")
    public ResponseEntity<PaginatedResponse<ClientResponse>> getPaginatedClients(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.getPaginatedClients(pageable));
    }

    @PostMapping("/create/client")
    public ResponseEntity<RegisterClientResponse> createClient(@Valid @RequestBody RegisterClientRequest request) {
        RegisterClientResponse client = adminService.createClient(request);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request) {
        return ResponseEntity.ok(adminService.updateClient(id, request));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        adminService.deleteClient(id);
        return ResponseEntity.ok().build();
    }
}
