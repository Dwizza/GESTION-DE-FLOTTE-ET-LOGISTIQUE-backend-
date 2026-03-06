package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.DeliveryStatus;
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
public class DeliveryResponse {
    private UUID id;
    private String reference;
    private BigDecimal weight;
    private BigDecimal volume;
    private BigDecimal prix;
    private String pickupAddress;
    private String deliveryAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private SimpleTripResponse trip;
    private SimpleCategoryResponse category;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleTripResponse {
        private UUID id;
        private String reference;
        private com.fleet_management_backend.entity.enums.TripStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleCategoryResponse {
        private UUID id;
        private String name;
    }
}
