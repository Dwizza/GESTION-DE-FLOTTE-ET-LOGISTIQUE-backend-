package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.entity.enums.TrailerType;
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
public class TrailerResponse {
    private UUID id;
    private TrailerType type;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private TrailerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
