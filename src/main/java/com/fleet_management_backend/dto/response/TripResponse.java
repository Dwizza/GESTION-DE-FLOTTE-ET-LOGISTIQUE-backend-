package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private UUID id;
    private String reference;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDistance;
    private TripStatus status;
    private UUID createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relational simple responses
    private SimpleDriverResponse driver;
    private SimpleClientResponse client;

    // Attached Resources
    private List<SimpleTruckResponse> trucks;
    private List<SimpleTrailerResponse> trailers;
    private List<SimpleDeliveryResponse> deliveries;

    // Inner DTOs to avoid circular dependencies and huge responses
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDriverResponse {
        private UUID id;
        private String licenseNumber;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleClientResponse {
        private UUID id;
        private String companyName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleTruckResponse {
        private UUID id;
        private String registrationNumber;
        private String brand;
        private com.fleet_management_backend.entity.enums.TruckStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleTrailerResponse {
        private UUID id;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDeliveryResponse {
        private UUID id;
        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;
        private String deliveryAddress;
        private Double deliveryLatitude;
        private Double deliveryLongitude;
        private BigDecimal weight;
        private BigDecimal volume;
    }
}
