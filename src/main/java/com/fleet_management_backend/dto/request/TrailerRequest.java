package com.fleet_management_backend.dto.request;

import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.entity.enums.TrailerType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrailerRequest {

    @NotNull(message = "Type is required")
    private TrailerType type;

    @PositiveOrZero(message = "Max weight must be zero or positive")
    private BigDecimal maxWeight;

    @PositiveOrZero(message = "Max volume must be zero or positive")
    private BigDecimal maxVolume;

    @NotNull(message = "Status is required")
    private TrailerStatus status;
}
