package com.fleet_management_backend.dto.request;

import com.fleet_management_backend.entity.enums.TruckStatus;
import jakarta.validation.constraints.NotBlank;
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
public class TruckRequest {
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Status is required")
    private TruckStatus status;

    @PositiveOrZero(message = "Total mileage must be zero or positive")
    private BigDecimal totalMileage;
}
