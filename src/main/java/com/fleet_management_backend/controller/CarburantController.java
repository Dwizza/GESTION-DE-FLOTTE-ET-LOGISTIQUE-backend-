package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.CarburantTransactionRequest;
import com.fleet_management_backend.dto.response.CarburantTransactionResponse;
import com.fleet_management_backend.service.CarburantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carburants")
@RequiredArgsConstructor
public class CarburantController {

    private final CarburantService carburantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<CarburantTransactionResponse> create(
            @Valid @RequestBody CarburantTransactionRequest request) {
        return new ResponseEntity<>(carburantService.createTransaction(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<List<CarburantTransactionResponse>> getAll() {
        return ResponseEntity.ok(carburantService.getAllTransactions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<CarburantTransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(carburantService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<CarburantTransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CarburantTransactionRequest request) {
        return ResponseEntity.ok(carburantService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        carburantService.deleteTransaction(id);
        return ResponseEntity.ok().build();
    }
}
