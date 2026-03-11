package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.response.AnalyticsResponse;
import com.fleet_management_backend.entity.enums.TripStatus;
import com.fleet_management_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

        private final TruckRepository truckRepository;
        private final TrailerRepository trailerRepository;
        private final DriverRepository driverRepository;
        private final TripRepository tripRepository;
        private final MaintenanceRepository maintenanceRepository;
        private final CarburantTransactionRepository carburantRepository;
        private final DeliveryRepository deliveryRepository;

        // Removing PRICE_PER_KM since we use direct delivery prices now

        public AnalyticsResponse getDashboardMetrics() {
                AnalyticsResponse response = new AnalyticsResponse();

                // --- Totals ---
                response.setTotalTrucks(truckRepository.count());
                response.setTotalTrailers(trailerRepository.count());
                response.setTotalDrivers(driverRepository.count());

                long availableDrivers = driverRepository.findAll().stream()
                                .filter(d -> Boolean.TRUE.equals(d.getAvailable()))
                                .count();
                response.setAvailableDrivers(availableDrivers);

                long activeTrips = tripRepository.findAll().stream()
                                .filter(t -> t.getStatus() == TripStatus.ONGOING)
                                .count();
                response.setActiveTrips(activeTrips);

                // --- Financial ---
                BigDecimal totalRevenue = deliveryRepository.findAll().stream()
                                .map(d -> d.getPrix() != null ? d.getPrix() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotalRevenue(totalRevenue);

                BigDecimal totalFuelCost = carburantRepository.findAll().stream()
                                .map(c -> c.getCout() != null ? c.getCout() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotalFuelCost(totalFuelCost);

                BigDecimal totalMaintenanceCost = maintenanceRepository.findAll().stream()
                                .map(m -> m.getCout() != null ? m.getCout() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotalMaintenanceCost(totalMaintenanceCost);

                BigDecimal totalExpenses = totalFuelCost.add(totalMaintenanceCost);
                response.setTotalProfit(totalRevenue.subtract(totalExpenses));

                // --- Chart: Trucks by Status ---
                Map<String, Long> trucksByStatus = truckRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                                t -> t.getStatus().name(),
                                                Collectors.counting()));
                response.setTrucksByStatus(trucksByStatus);

                // --- Chart: Trips by Status ---
                Map<String, Long> tripsByStatus = tripRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                                t -> t.getStatus().name(),
                                                Collectors.counting()));
                response.setTripsByStatus(tripsByStatus);

                // --- Chart: Trucks by Brand ---
                Map<String, Long> trucksByBrand = truckRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                                t -> t.getBrand() != null ? t.getBrand() : "Autre",
                                                Collectors.counting()));
                response.setTrucksByBrand(trucksByBrand);

                // --- Chart: Trailers by Type ---
                Map<String, Long> trailersByType = trailerRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                                t -> t.getType().name(),
                                                Collectors.counting()));
                response.setTrailersByType(trailersByType);

                return response;
        }
}
