package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.TrailerRequest;
import com.fleet_management_backend.dto.response.TrailerResponse;
import com.fleet_management_backend.service.TrailerService;
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
@RequestMapping("/api/admin/trailers")
@RequiredArgsConstructor
public class TrailerController {

    private final TrailerService trailerService;

    @PostMapping
    public ResponseEntity<TrailerResponse> createTrailer(@Valid @RequestBody TrailerRequest request) {
        return new ResponseEntity<>(trailerService.createTrailer(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TrailerResponse>> getAllTrailers() {
        return ResponseEntity.ok(trailerService.getAllTrailers());
    }

    @GetMapping("/page")
    public ResponseEntity<PaginatedResponse<TrailerResponse>> getPaginatedTrailers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(trailerService.getPaginatedTrailers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrailerResponse> getTrailerById(@PathVariable UUID id) {
        return ResponseEntity.ok(trailerService.getTrailerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrailerResponse> updateTrailer(@PathVariable UUID id,
            @Valid @RequestBody TrailerRequest request) {
        return ResponseEntity.ok(trailerService.updateTrailer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrailer(@PathVariable UUID id) {
        trailerService.deleteTrailer(id);
        return ResponseEntity.noContent().build();
    }
}
