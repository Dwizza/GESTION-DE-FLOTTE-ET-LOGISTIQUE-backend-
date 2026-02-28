package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.request.UpdateDriverRequest;
import com.fleet_management_backend.dto.response.DriverResponse;
import com.fleet_management_backend.dto.response.RegisterDriverResponse;
import com.fleet_management_backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager")
@AllArgsConstructor
public class ManagerController {

    private final DriverService driverService;

    @PostMapping("/create/driver")
    public ResponseEntity<RegisterDriverResponse> createDriver(@Valid @RequestBody RegisterDriverRequest request) {
        return ResponseEntity.ok(driverService.createDriver(request));
    }

    @DeleteMapping("/delete/driver/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable UUID id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @PutMapping("/update/driver/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(driverService.updateDriver(id, request));
    }
}
