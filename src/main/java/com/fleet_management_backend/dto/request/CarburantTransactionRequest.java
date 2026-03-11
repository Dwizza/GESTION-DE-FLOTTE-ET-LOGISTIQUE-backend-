package com.fleet_management_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CarburantTransactionRequest {

    @NotNull(message = "Reference is required")
    private String reference;

    @NotNull(message = "Datetime is required")
    private LocalDateTime dateHeure;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantite;

    @NotNull(message = "Cost is required")
    private BigDecimal cout;

    @NotNull(message = "Station name is required")
    private String stationName;

    private String receiptNumber;

    @NotNull(message = "Truck ID is required")
    private UUID truckId;
}
