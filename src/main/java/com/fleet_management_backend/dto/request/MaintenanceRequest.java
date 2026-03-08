package com.fleet_management_backend.dto.request;

import com.fleet_management_backend.entity.enums.MaintenanceStatus;
import com.fleet_management_backend.entity.enums.MaintenanceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceRequest {

    @NotNull(message = "Type is required")
    private MaintenanceType type;

    @NotNull(message = "Date is required")
    private LocalDate dateMaintenance;

    @NotNull(message = "Cost is required")
    private BigDecimal cout;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status;

    private String reference;

    private String description;

    private String performedBy;

    private UUID truckId;

    private UUID trailerId;
}
