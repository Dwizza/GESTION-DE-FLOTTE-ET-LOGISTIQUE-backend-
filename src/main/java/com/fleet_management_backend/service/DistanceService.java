package com.fleet_management_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Service to calculate real road distance between two GPS coordinates
 * using the free OSRM (Open Source Routing Machine) API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceService {

    private final RestTemplate restTemplate;

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculate the road distance between two points using OSRM.
     * Falls back to Haversine (straight-line) if OSRM is unavailable.
     *
     * @return distance in kilometers
     */
    public BigDecimal calculateRoadDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
            String coords = lon1 + "," + lat1 + ";" + lon2 + "," + lat2;
            String url = "https://router.project-osrm.org/route/v1/driving/" + coords + "?overview=false";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    // OSRM returns distance in meters
                    Number distanceMeters = (Number) routes.get(0).get("distance");
                    BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters.doubleValue())
                            .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
                    log.info("OSRM road distance: {} km (from [{},{}] to [{},{}])", distanceKm, lat1, lon1, lat2, lon2);
                    return distanceKm;
                }
            }

            log.warn("OSRM returned no routes, falling back to Haversine");
            return calculateHaversineDistance(lat1, lon1, lat2, lon2);

        } catch (Exception e) {
            log.warn("OSRM API call failed, falling back to Haversine: {}", e.getMessage());
            return calculateHaversineDistance(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Haversine fallback: straight-line distance between two GPS points.
     */
    private BigDecimal calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = EARTH_RADIUS_KM * c;
        return BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP);
    }
}
