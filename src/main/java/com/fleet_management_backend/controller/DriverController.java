package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.response.TripResponse;
import com.fleet_management_backend.entity.Driver;
import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.repository.DriverRepository;
import com.fleet_management_backend.repository.UserRepository;
import com.fleet_management_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final TripService tripService;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    @GetMapping("/trips")
    public ResponseEntity<List<TripResponse>> getDriverTrips(Authentication authentication) {
        Driver driver = getAuthenticatedDriver(authentication);
        return ResponseEntity.ok(tripService.getTripsByDriverId(driver.getId()));
    }

    @GetMapping("/trips/page")
    public ResponseEntity<PaginatedResponse<TripResponse>> getPaginatedDriverTrips(
            Authentication authentication,
            @PageableDefault(size = 9) Pageable pageable) {
        Driver driver = getAuthenticatedDriver(authentication);
        return ResponseEntity.ok(tripService.getPaginatedTripsByDriverId(driver.getId(), pageable));
    }

    @PostMapping("/trips/{id}/accept")
    public ResponseEntity<TripResponse> acceptTrip(@PathVariable UUID id, Authentication authentication) {
        Driver driver = getAuthenticatedDriver(authentication);
        return ResponseEntity.ok(tripService.acceptTrip(id, driver.getId()));
    }

    @PostMapping("/trips/{id}/refuse")
    public ResponseEntity<TripResponse> refuseTrip(@PathVariable UUID id, Authentication authentication) {
        Driver driver = getAuthenticatedDriver(authentication);
        return ResponseEntity.ok(tripService.refuseTrip(id, driver.getId()));
    }

    @PostMapping("/trips/{id}/complete")
    public ResponseEntity<TripResponse> completeTrip(@PathVariable UUID id, Authentication authentication) {
        Driver driver = getAuthenticatedDriver(authentication);
        return ResponseEntity.ok(tripService.completeTrip(id, driver.getId()));
    }

    private Driver getAuthenticatedDriver(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found for user"));
    }
}
