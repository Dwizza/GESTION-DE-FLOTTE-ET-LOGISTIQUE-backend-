package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.MaintenanceRequest;
import com.fleet_management_backend.dto.response.MaintenanceResponse;
import com.fleet_management_backend.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<MaintenanceResponse> create(@Valid @RequestBody MaintenanceRequest request) {
        return new ResponseEntity<>(maintenanceService.createMaintenance(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<List<MaintenanceResponse>> getAll() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenances());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<PaginatedResponse<MaintenanceResponse>> getPaginated(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(maintenanceService.getPaginatedMaintenances(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<MaintenanceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(maintenanceService.getMaintenanceById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<MaintenanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.updateMaintenance(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        maintenanceService.deleteMaintenance(id);
        return ResponseEntity.noContent().build();
    }
}
