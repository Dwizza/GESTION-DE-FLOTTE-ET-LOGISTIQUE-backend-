package com.fleet_management_backend.dto.request;

import com.fleet_management_backend.entity.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripRequest {

    @NotBlank(message = "Reference is required")
    private String reference;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Status is required")
    private TripStatus status;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    // Optional arrays for attaching resources directly upon creation/update
    private List<UUID> truckIds;
    private List<UUID> trailerIds;
}
