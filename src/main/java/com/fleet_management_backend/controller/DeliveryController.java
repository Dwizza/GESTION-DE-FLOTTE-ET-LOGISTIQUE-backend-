package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.DeliveryRequest;
import com.fleet_management_backend.dto.response.DeliveryResponse;
import com.fleet_management_backend.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody DeliveryRequest request) {
        return new ResponseEntity<>(deliveryService.createDelivery(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/page")
    public ResponseEntity<PaginatedResponse<DeliveryResponse>> getPaginatedDeliveries(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(deliveryService.getPaginatedDeliveries(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryResponse> updateDelivery(@PathVariable UUID id,
            @Valid @RequestBody DeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.updateDelivery(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable UUID id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }
}
