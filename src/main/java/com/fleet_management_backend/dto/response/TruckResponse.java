package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.TruckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckResponse {
    private UUID id;
    private String registrationNumber;
    private String brand;
    private TruckStatus status;
    private BigDecimal totalMileage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
