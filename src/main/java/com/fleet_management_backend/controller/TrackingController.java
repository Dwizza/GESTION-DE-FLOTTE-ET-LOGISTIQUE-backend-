package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.TrackingRequest;
import com.fleet_management_backend.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.fleet_management_backend.dto.response.TrackingResponse;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<Void> recordTracking(@Valid @RequestBody TrackingRequest request) {
        trackingService.recordTrackingPoint(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<TrackingResponse>> getTripPath(@PathVariable UUID tripId) {
        return ResponseEntity.ok(trackingService.getTripPath(tripId));
    }

    @GetMapping("/live")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<TrackingResponse>> getLiveTracking() {
        return ResponseEntity.ok(trackingService.getLivePositions());
    }
}
