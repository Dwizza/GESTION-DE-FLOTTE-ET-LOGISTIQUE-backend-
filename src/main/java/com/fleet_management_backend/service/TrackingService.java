package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TrackingRequest;
import com.fleet_management_backend.dto.response.TrackingResponse;
import com.fleet_management_backend.entity.TrackingPoint;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.entity.Truck;
import com.fleet_management_backend.entity.enums.TripStatus;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.repository.TrackingPointRepository;
import com.fleet_management_backend.repository.TripRepository;
import com.fleet_management_backend.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingPointRepository trackingPointRepository;
    private final TruckRepository truckRepository;
    private final TripRepository tripRepository;
    private final MaintenanceService maintenanceService;

    private static final int EARTH_RADIUS_KM = 6371;

    @Transactional
    public void recordTrackingPoint(TrackingRequest request) {
        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        Optional<TrackingPoint> lastPointOpt = trackingPointRepository
                .findTopByTruckIdOrderByTimestampDesc(truck.getId());
        double distanceKm = 0.0;

        if (lastPointOpt.isPresent()) {
            TrackingPoint lastPoint = lastPointOpt.get();
            distanceKm = calculateHaversineDistance(
                    lastPoint.getLatitude().doubleValue(), lastPoint.getLongitude().doubleValue(),
                    request.getLatitude().doubleValue(), request.getLongitude().doubleValue());
        }

        TrackingPoint newPoint = new TrackingPoint();
        newPoint.setTruck(truck);
        newPoint.setLatitude(request.getLatitude());
        newPoint.setLongitude(request.getLongitude());
        newPoint.setTimestamp(LocalDateTime.now());

        // Associate with ongoing trip if exists
        List<Trip> ongoingTrips = tripRepository.findTripsByTruckIdAndStatus(truck.getId(), TripStatus.ONGOING);
        if (!ongoingTrips.isEmpty()) {
            newPoint.setTrip(ongoingTrips.get(0));
        }

        trackingPointRepository.save(newPoint);

        if (distanceKm > 0) {
            BigDecimal distanceBd = BigDecimal.valueOf(distanceKm);

            BigDecimal currentMileage = truck.getTotalMileage() != null ? truck.getTotalMileage() : BigDecimal.ZERO;
            BigDecimal newMileage = currentMileage.add(distanceBd);

            // Auto Maintenance trigger every 50,000 km
            maintenanceService.triggerAutoMaintenance(truck, currentMileage, newMileage);

            truck.setTotalMileage(newMileage);
            truckRepository.save(truck);

            for (Trip trip : ongoingTrips) {
                BigDecimal currentTripDist = trip.getTotalDistance() != null ? trip.getTotalDistance()
                        : BigDecimal.ZERO;
                trip.setTotalDistance(currentTripDist.add(distanceBd));
                tripRepository.save(trip);
            }
        }
    }

    public List<TrackingResponse> getTripPath(UUID tripId) {
        return trackingPointRepository.findByTripIdOrderByTimestampAsc(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TrackingResponse> getLivePositions() {
        return trackingPointRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TrackingResponse mapToResponse(TrackingPoint point) {
        return TrackingResponse.builder()
                .id(point.getId())
                .latitude(point.getLatitude())
                .longitude(point.getLongitude())
                .timestamp(point.getTimestamp())
                .truckId(point.getTruck().getId())
                .tripId(point.getTrip() != null ? point.getTrip().getId() : null)
                .build();
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
