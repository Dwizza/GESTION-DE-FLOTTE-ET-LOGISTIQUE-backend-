package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.MaintenanceStatus;
import com.fleet_management_backend.entity.enums.MaintenanceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceResponse {
    private UUID id;
    private MaintenanceType type;
    private LocalDate dateMaintenance;
    private BigDecimal cout;
    private MaintenanceStatus status;
    private UUID truckId;
    private UUID trailerId;

    private String reference;
    private String description;
    private String performedBy;

    private SimpleTruckResponse truck;
    private SimpleTrailerResponse trailer;

    @Data
    public static class SimpleTruckResponse {
        private UUID id;
        private String registrationNumber;
        private String brand;
    }

    @Data
    public static class SimpleTrailerResponse {
        private UUID id;
        private String type;
    }
}
