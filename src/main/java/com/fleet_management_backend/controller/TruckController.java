package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.TruckRequest;
import com.fleet_management_backend.dto.response.TruckResponse;
import com.fleet_management_backend.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @PostMapping
    public ResponseEntity<TruckResponse> createTruck(@Valid @RequestBody TruckRequest request) {
        return new ResponseEntity<>(truckService.createTruck(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TruckResponse>> getAllTrucks() {
        return ResponseEntity.ok(truckService.getAllTrucks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TruckResponse> getTruckById(@PathVariable UUID id) {
        return ResponseEntity.ok(truckService.getTruckById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TruckResponse> updateTruck(@PathVariable UUID id, @Valid @RequestBody TruckRequest request) {
        return ResponseEntity.ok(truckService.updateTruck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTruck(@PathVariable UUID id) {
        truckService.deleteTruck(id);
        return ResponseEntity.noContent().build();
    }
}
