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
                response.setAvailableDrivers(driverRepository.countByAvailableTrue());
                response.setActiveTrips(tripRepository.countByStatus(TripStatus.ONGOING));

                // --- Financial ---
                BigDecimal totalRevenue = deliveryRepository.sumTotalRevenue();
                response.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

                BigDecimal totalFuelCost = carburantRepository.sumTotalFuelCost();
                response.setTotalFuelCost(totalFuelCost != null ? totalFuelCost : BigDecimal.ZERO);

                BigDecimal totalMaintenanceCost = maintenanceRepository.sumTotalMaintenanceCost();
                response.setTotalMaintenanceCost(totalMaintenanceCost != null ? totalMaintenanceCost : BigDecimal.ZERO);

                response.setTotalProfit(response.getTotalRevenue().subtract(
                        response.getTotalFuelCost().add(response.getTotalMaintenanceCost())));

                // --- Charts (Optimized Grouping) ---
                response.setTrucksByStatus(convertListToMap(truckRepository.countTrucksByStatus()));
                response.setTripsByStatus(convertListToMap(tripRepository.countTripsByStatus()));
                response.setTrucksByBrand(convertListToMap(truckRepository.countTrucksByBrand()));
                response.setTrailersByType(convertListToMap(trailerRepository.countTrailersByType()));

                return response;
        }

        private Map<String, Long> convertListToMap(java.util.List<Object[]> results) {
                return results.stream()
                                .collect(Collectors.toMap(
                                                row -> row[0].toString(),
                                                row -> (Long) row[1]));
        }
}
