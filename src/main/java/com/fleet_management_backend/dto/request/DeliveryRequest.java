package com.fleet_management_backend.dto.request;

import com.fleet_management_backend.entity.enums.DeliveryStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {

    private String reference;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    private BigDecimal weight;

    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume must be greater than zero")
    private BigDecimal volume;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal prix;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    @NotNull(message = "Status is required")
    private DeliveryStatus status;

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    private UUID categoryId; // Optional depending on requirements, let's keep it optional but usually needed
}
