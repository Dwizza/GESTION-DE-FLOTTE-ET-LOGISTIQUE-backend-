package com.fleet_management_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CarburantTransactionResponse {
    private UUID id;
    private String reference;
    private LocalDateTime dateHeure;
    private BigDecimal quantite;
    private BigDecimal cout;
    private String stationName;
    private String receiptNumber;
    private UUID truckId;
}
