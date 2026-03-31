package com.fleet_management_backend.service;

import com.fleet_management_backend.entity.Trailer;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CapacityValidationService {

    public void validateTripCapacity(Trip trip, BigDecimal newWeight, BigDecimal newVolume, UUID excludingDeliveryId) {
        BigDecimal totalWeight = trip.getDeliveries().stream()
                .filter(d -> excludingDeliveryId == null || !d.getId().equals(excludingDeliveryId))
                .map(d -> d.getWeight() != null ? d.getWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(newWeight != null ? newWeight : BigDecimal.ZERO);

        BigDecimal totalVolume = trip.getDeliveries().stream()
                .filter(d -> excludingDeliveryId == null || !d.getId().equals(excludingDeliveryId))
                .map(d -> d.getVolume() != null ? d.getVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(newVolume != null ? newVolume : BigDecimal.ZERO);

        BigDecimal maxWeight = trip.getTripTrailers().stream()
                .map(tt -> tt.getTrailer() != null && tt.getTrailer().getMaxWeight() != null ? tt.getTrailer().getMaxWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maxVolume = trip.getTripTrailers().stream()
                .map(tt -> tt.getTrailer() != null && tt.getTrailer().getMaxVolume() != null ? tt.getTrailer().getMaxVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(maxWeight) > 0) {
            throw new ConflictException("Trip weight capacity exceeded. Total: " + totalWeight + ", Max: " + maxWeight);
        }
        if (totalVolume.compareTo(maxVolume) > 0) {
            throw new ConflictException("Trip volume capacity exceeded. Total: " + totalVolume + ", Max: " + maxVolume);
        }
    }

    public void validateTrailerCapacityForExistingDeliveries(Trip trip, List<Trailer> newTrailers) {
        BigDecimal totalWeight = trip.getDeliveries().stream()
                .map(d -> d.getWeight() != null ? d.getWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVolume = trip.getDeliveries().stream()
                .map(d -> d.getVolume() != null ? d.getVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maxWeight = newTrailers.stream()
                .map(t -> t.getMaxWeight() != null ? t.getMaxWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maxVolume = newTrailers.stream()
                .map(t -> t.getMaxVolume() != null ? t.getMaxVolume() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(maxWeight) > 0) {
            throw new ConflictException(
                    "Assigned trailers do not have enough weight capacity for the existing deliveries. Total needed: "
                            + totalWeight + ", Available: " + maxWeight);
        }
        if (totalVolume.compareTo(maxVolume) > 0) {
            throw new ConflictException(
                    "Assigned trailers do not have enough volume capacity for the existing deliveries. Total needed: "
                            + totalVolume + ", Available: " + maxVolume);
        }
    }
}
