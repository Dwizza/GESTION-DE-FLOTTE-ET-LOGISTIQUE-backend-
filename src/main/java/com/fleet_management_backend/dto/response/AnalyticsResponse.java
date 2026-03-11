package com.fleet_management_backend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class AnalyticsResponse {
    private long totalTrucks;
    private long totalTrailers;
    private long totalDrivers;
    private long availableDrivers;
    private long activeTrips;
    private BigDecimal totalRevenue;
    private BigDecimal totalFuelCost;
    private BigDecimal totalMaintenanceCost;
    private BigDecimal totalProfit;

    // Chart data
    private Map<String, Long> trucksByStatus;
    private Map<String, Long> tripsByStatus;
    private Map<String, Long> trucksByBrand;
    private Map<String, Long> trailersByType;
}
