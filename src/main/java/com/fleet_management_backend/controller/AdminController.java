package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.request.RegisterManagerRequest;
import com.fleet_management_backend.dto.response.RegisterDriverResponse;
import com.fleet_management_backend.dto.response.RegisterManagerResponse;
import com.fleet_management_backend.service.AdminService;
import com.fleet_management_backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/admin")
@RestController
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DriverService driverService;

    @PostMapping("/create/manager")
    public ResponseEntity<RegisterManagerResponse> createManager(@Valid @RequestBody RegisterManagerRequest request) {
        RegisterManagerResponse manager = adminService.CreateManager(request);
        return ResponseEntity.ok(manager);
    }

    @DeleteMapping("/delete/manager/{id}")
    public ResponseEntity<Void> DeleteManager(@PathVariable UUID id) {
        adminService.DeleteManager(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/driver")
    public ResponseEntity<RegisterDriverResponse> CreateDriver(@Valid @RequestBody RegisterDriverRequest request) {
        RegisterDriverResponse driver = driverService.createDriver(request);
        return ResponseEntity.ok(driver);
    }

    @DeleteMapping("/delete/driver/{id}")
    public ResponseEntity<Void> DeleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/managers")
    public ResponseEntity<java.util.List<com.fleet_management_backend.dto.response.ManagerResponse>> getAllManagers() {
        return ResponseEntity.ok(adminService.getAllManagers());
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
}
