package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.response.AnalyticsResponse;
import com.fleet_management_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    public ResponseEntity<AnalyticsResponse> getDashboardMetrics() {
        return ResponseEntity.ok(analyticsService.getDashboardMetrics());
    }
}
