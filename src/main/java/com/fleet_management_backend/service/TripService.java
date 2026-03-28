package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TripRequest;
import com.fleet_management_backend.dto.response.PaginatedResponse;
import com.fleet_management_backend.dto.response.TripResponse;
import com.fleet_management_backend.entity.*;
import com.fleet_management_backend.entity.enums.DeliveryStatus;
import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.entity.enums.TripStatus;
import com.fleet_management_backend.entity.enums.TruckStatus;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.TripMapper;
import com.fleet_management_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final ClientRepository clientRepository;
    private final TruckRepository truckRepository;
    private final TrailerRepository trailerRepository;
    private final DeliveryRepository deliveryRepository;
    private final TripMapper tripMapper;
    private final DistanceService distanceService;

    @Transactional
    public TripResponse createTrip(TripRequest request, UUID createdByUserId) {
        if (request.getReference() == null || request.getReference().trim().isEmpty()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            request.setReference("TRP-" + timestamp + "-" + random);
        } else if (tripRepository.existsByReference(request.getReference())) {
            throw new ConflictException("Trip with reference " + request.getReference() + " already exists.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (!driver.getAvailable()) {
            throw new ConflictException("Driver is currently not available for a new trip.");
        }

        driver.setAvailable(false);
        driverRepository.save(driver);

        Trip trip = tripMapper.toEntity(request);
        trip.setClient(client);
        trip.setDriver(driver);
        trip.setCreatedByUserId(createdByUserId);
        trip.setTotalDistance(BigDecimal.ZERO);

        if (request.getTruckIds() != null && !request.getTruckIds().isEmpty()) {
            for (UUID truckId : request.getTruckIds()) {
                Truck truck = truckRepository.findById(truckId)
                        .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

                if (request.getStatus() != TripStatus.COMPLETED && request.getStatus() != TripStatus.CANCELLED) {
                    if (truck.getStatus() != TruckStatus.AVAILABLE) {
                        throw new ConflictException("Truck " + truck.getRegistrationNumber()
                                + " is currently not available (Status: " + truck.getStatus() + ").");
                    }
                    truck.setStatus(TruckStatus.IN_TRIP);
                    truckRepository.save(truck);
                }

                TripTruck tripTruck = new TripTruck();
                tripTruck.setTrip(trip);
                tripTruck.setTruck(truck);
                tripTruck.setReference(request.getReference() + "-TRK-" + truck.getRegistrationNumber());
                trip.getTripTrucks().add(tripTruck);
            }
        }

        if (request.getTrailerIds() != null && !request.getTrailerIds().isEmpty()) {
            for (UUID trailerId : request.getTrailerIds()) {
                Trailer trailer = trailerRepository.findById(trailerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Trailer not found"));

                if (request.getStatus() != TripStatus.COMPLETED && request.getStatus() != TripStatus.CANCELLED) {
                    if (trailer.getStatus() != TrailerStatus.AVAILABLE) {
                        throw new ConflictException("Trailer is currently not available.");
                    }
                    trailer.setStatus(TrailerStatus.IN_USE);
                    trailerRepository.save(trailer);
                }

                TripTrailer tripTrailer = new TripTrailer();
                tripTrailer.setTrip(trip);
                tripTrailer.setTrailer(trailer);
                tripTrailer.setReference(request.getReference() + "-TRL-" + trailerId.toString().substring(0, 4));
                trip.getTripTrailers().add(tripTrailer);
            }
        }

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(savedTrip);
    }

    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TripResponse getTripById(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        return tripMapper.toResponse(trip);
    }

    public List<TripResponse> getTripsByDriverId(UUID driverId) {
        return tripRepository.findByDriverIdOrderByStartDateDesc(driverId).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<TripResponse> getPaginatedTrips(Pageable pageable) {
        Page<Trip> tripsPage = tripRepository.findAll(pageable);
        return PaginatedResponse.<TripResponse>builder()
                .content(tripsPage.getContent().stream().map(tripMapper::toResponse).toList())
                .pageNumber(tripsPage.getNumber())
                .pageSize(tripsPage.getSize())
                .totalElements(tripsPage.getTotalElements())
                .totalPages(tripsPage.getTotalPages())
                .last(tripsPage.isLast())
                .build();
    }

    public PaginatedResponse<TripResponse> getPaginatedTripsByDriverId(UUID driverId, Pageable pageable) {
        Page<Trip> tripsPage = tripRepository.findByDriverId(driverId, pageable);
        return PaginatedResponse.<TripResponse>builder()
                .content(tripsPage.getContent().stream().map(tripMapper::toResponse).toList())
                .pageNumber(tripsPage.getNumber())
                .pageSize(tripsPage.getSize())
                .totalElements(tripsPage.getTotalElements())
                .totalPages(tripsPage.getTotalPages())
                .last(tripsPage.isLast())
                .build();
    }

    @Transactional
    public TripResponse updateTrip(UUID id, TripRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (request.getReference() != null && !request.getReference().trim().isEmpty()
                && !trip.getReference().equals(request.getReference())) {
            if (tripRepository.existsByReference(request.getReference())) {
                throw new ConflictException("Trip with reference " + request.getReference() + " already exists.");
            }
            trip.setReference(request.getReference());
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Driver newDriver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        Driver oldDriver = trip.getDriver();

        boolean isNowFinished = request.getStatus() == TripStatus.COMPLETED
                || request.getStatus() == TripStatus.CANCELLED;
        boolean wasFinished = trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED;

        if (!oldDriver.getId().equals(newDriver.getId())) {
            if (!newDriver.getAvailable() && !isNowFinished) {
                throw new ConflictException("New driver is currently not available.");
            }
            if (!wasFinished) {
                oldDriver.setAvailable(true);
                driverRepository.save(oldDriver);
            }
            if (!isNowFinished) {
                newDriver.setAvailable(false);
                driverRepository.save(newDriver);
            }
        } else {
            if (isNowFinished && !wasFinished) {
                newDriver.setAvailable(true);
                driverRepository.save(newDriver);
            } else if (!isNowFinished && wasFinished) {
                if (!newDriver.getAvailable()) {
                    throw new ConflictException("Driver is currently not available to resume this trip.");
                }
                newDriver.setAvailable(false);
                driverRepository.save(newDriver);
            }
        }

        // Reference is preserved — only updated above if a new one was explicitly provided
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setStatus(request.getStatus());

        if (request.getStatus() == TripStatus.COMPLETED && !wasFinished) {
            java.util.List<Delivery> deliveries = deliveryRepository.findByTripId(trip.getId());
            if (deliveries != null && !deliveries.isEmpty()) {
                deliveries.forEach(d -> d.setStatus(DeliveryStatus.DELIVERED));
                deliveryRepository.saveAll(deliveries);
            }

            // Auto-calculate total road distance from deliveries and update truck mileage
            BigDecimal totalDistance = calculateTotalDeliveryDistance(trip.getId());
            if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                trip.setTotalDistance(totalDistance);
                log.info("Trip {} completed. Total delivery distance: {} km", trip.getReference(), totalDistance);
            }
        } else if (request.getStatus() == TripStatus.ONGOING) {
            java.util.List<Delivery> deliveries = deliveryRepository.findByTripId(trip.getId());
            if (deliveries != null && !deliveries.isEmpty()) {
                deliveries.forEach(d -> d.setStatus(DeliveryStatus.IN_PROGRESS));
                deliveryRepository.saveAll(deliveries);
            }
        }
        trip.setClient(client);
        trip.setDriver(newDriver);

        for (TripTruck tt : trip.getTripTrucks()) {
            Truck oldTruck = tt.getTruck();
            if (oldTruck.getStatus() == TruckStatus.IN_TRIP) {
                oldTruck.setStatus(TruckStatus.AVAILABLE);

                // Update truck mileage when trip is completed
                if (isNowFinished && !wasFinished && request.getStatus() == TripStatus.COMPLETED
                        && trip.getTotalDistance() != null && trip.getTotalDistance().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal currentMileage = oldTruck.getTotalMileage() != null ? oldTruck.getTotalMileage() : BigDecimal.ZERO;
                    oldTruck.setTotalMileage(currentMileage.add(trip.getTotalDistance()));
                    log.info("Truck {} mileage updated: {} -> {} km", oldTruck.getRegistrationNumber(),
                            currentMileage, oldTruck.getTotalMileage());
                }

                truckRepository.save(oldTruck);
            }
        }
        trip.getTripTrucks().clear();
        if (request.getTruckIds() != null && !request.getTruckIds().isEmpty()) {
            for (UUID truckId : request.getTruckIds()) {
                Truck truck = truckRepository.findById(truckId)
                        .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

                if (!isNowFinished) {
                    if (truck.getStatus() != TruckStatus.AVAILABLE && truck.getStatus() != TruckStatus.IN_TRIP) {
                        throw new ConflictException(
                                "Truck " + truck.getRegistrationNumber() + " is currently not available.");
                    }
                    truck.setStatus(TruckStatus.IN_TRIP);
                    truckRepository.save(truck);
                }

                TripTruck tripTruck = new TripTruck();
                tripTruck.setTrip(trip);
                tripTruck.setTruck(truck);
                tripTruck.setReference(trip.getReference() + "-TRK-" + truck.getRegistrationNumber());
                trip.getTripTrucks().add(tripTruck);
            }
        }

        for (TripTrailer tt : trip.getTripTrailers()) {
            Trailer oldTrailer = tt.getTrailer();
            if (oldTrailer.getStatus() == TrailerStatus.IN_USE) {
                oldTrailer.setStatus(TrailerStatus.AVAILABLE);
                trailerRepository.save(oldTrailer);
            }
        }
        trip.getTripTrailers().clear();
        if (request.getTrailerIds() != null && !request.getTrailerIds().isEmpty()) {
            List<Trailer> updatedTrailers = request.getTrailerIds().stream()
                    .map(trailerId -> trailerRepository.findById(trailerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Trailer not found")))
                    .collect(Collectors.toList());

            validateDeliveriesCapacity(trip, updatedTrailers);

            for (Trailer trailer : updatedTrailers) {
                if (!isNowFinished) {
                    if (trailer.getStatus() != TrailerStatus.AVAILABLE && trailer.getStatus() != TrailerStatus.IN_USE) {
                        throw new ConflictException("Trailer is currently not available.");
                    }
                    trailer.setStatus(TrailerStatus.IN_USE);
                    trailerRepository.save(trailer);
                }

                TripTrailer tripTrailer = new TripTrailer();
                tripTrailer.setTrip(trip);
                tripTrailer.setTrailer(trailer);
                tripTrailer.setReference(trip.getReference() + "-TRL-" + trailer.getId().toString().substring(0, 4));
                trip.getTripTrailers().add(tripTrailer);
            }
        } else {
            validateDeliveriesCapacity(trip, List.of());
        }

        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(updatedTrip);
    }

    @Transactional
    public void deleteTrip(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getStatus() != TripStatus.COMPLETED && trip.getStatus() != TripStatus.CANCELLED) {
            Driver driver = trip.getDriver();
            driver.setAvailable(true);
            driverRepository.save(driver);
        }

        for (TripTruck tt : trip.getTripTrucks()) {
            Truck t = tt.getTruck();
            if (t.getStatus() == TruckStatus.IN_TRIP) {
                t.setStatus(TruckStatus.AVAILABLE);
                truckRepository.save(t);
            }
        }
        for (TripTrailer tl : trip.getTripTrailers()) {
            Trailer t = tl.getTrailer();
            if (t.getStatus() == TrailerStatus.IN_USE) {
                t.setStatus(TrailerStatus.AVAILABLE);
                trailerRepository.save(t);
            }
        }

        tripRepository.delete(trip);
    }

    @Transactional
    public TripResponse acceptTrip(UUID tripId, UUID driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new ConflictException("You are not assigned to this trip.");
        }

        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new ConflictException("Only planned trips can be accepted. Current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.ONGOING);

        java.util.List<Delivery> deliveries = deliveryRepository.findByTripId(trip.getId());
        if (deliveries != null && !deliveries.isEmpty()) {
            deliveries.forEach(d -> d.setStatus(DeliveryStatus.IN_PROGRESS));
            deliveryRepository.saveAll(deliveries);
        }

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(savedTrip);
    }

    @Transactional
    public TripResponse refuseTrip(UUID tripId, UUID driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new ConflictException("You are not assigned to this trip.");
        }

        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new ConflictException("Only planned trips can be refused. Current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.REFUSED_BY_DRIVER);

        // Release resources
        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driverRepository.save(driver);

        for (TripTruck tt : trip.getTripTrucks()) {
            Truck truck = tt.getTruck();
            if (truck.getStatus() == TruckStatus.IN_TRIP) {
                truck.setStatus(TruckStatus.AVAILABLE);
                truckRepository.save(truck);
            }
        }

        for (TripTrailer tt : trip.getTripTrailers()) {
            Trailer trailer = tt.getTrailer();
            if (trailer.getStatus() == TrailerStatus.IN_USE) {
                trailer.setStatus(TrailerStatus.AVAILABLE);
                trailerRepository.save(trailer);
            }
        }

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(savedTrip);
    }

    @Transactional
    public TripResponse completeTrip(UUID tripId, UUID driverId, BigDecimal distance) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new ConflictException("You are not assigned to this trip.");
        }

        if (trip.getStatus() != TripStatus.ONGOING) {
            throw new ConflictException("Only ongoing trips can be completed. Current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndDate(java.time.LocalDate.now());

        // Auto-calculate road distance from deliveries if not provided
        BigDecimal tripDistance = distance;
        if (tripDistance == null || tripDistance.compareTo(BigDecimal.ZERO) == 0) {
            tripDistance = calculateTotalDeliveryDistance(trip.getId());
        }
        if (tripDistance.compareTo(BigDecimal.ZERO) > 0) {
            trip.setTotalDistance(tripDistance);
            log.info("Trip {} completed by driver. Total distance: {} km", trip.getReference(), tripDistance);
        }

        java.util.List<Delivery> deliveries = deliveryRepository.findByTripId(trip.getId());
        if (deliveries != null && !deliveries.isEmpty()) {
            deliveries.forEach(d -> d.setStatus(DeliveryStatus.DELIVERED));
            deliveryRepository.saveAll(deliveries);
        }

        // Release resources
        Driver driver = trip.getDriver();
        driver.setAvailable(true);
        driverRepository.save(driver);

        for (TripTruck tt : trip.getTripTrucks()) {
            Truck truck = tt.getTruck();
            if (truck.getStatus() == TruckStatus.IN_TRIP) {
                truck.setStatus(TruckStatus.AVAILABLE);

                // Update mileage with trip distance
                if (tripDistance.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal m = truck.getTotalMileage() != null ? truck.getTotalMileage() : BigDecimal.ZERO;
                    truck.setTotalMileage(m.add(tripDistance));
                    log.info("Truck {} mileage updated: {} -> {} km",
                            truck.getRegistrationNumber(), m, truck.getTotalMileage());
                }

                truckRepository.save(truck);
            }
        }

        for (TripTrailer tt : trip.getTripTrailers()) {
            Trailer trailer = tt.getTrailer();
            if (trailer.getStatus() == TrailerStatus.IN_USE) {
                trailer.setStatus(TrailerStatus.AVAILABLE);
                trailerRepository.save(trailer);
            }
        }

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(savedTrip);
    }

    private void validateDeliveriesCapacity(Trip trip, List<Trailer> newTrailers) {
        if (trip.getDeliveries() == null || trip.getDeliveries().isEmpty())
            return;

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

    /**
     * Calculate the total road distance for all deliveries in a trip.
     * Uses OSRM API for real road distance, falls back to Haversine.
     */
    private BigDecimal calculateTotalDeliveryDistance(java.util.UUID tripId) {
        java.util.List<Delivery> deliveries = deliveryRepository.findByTripId(tripId);
        BigDecimal totalDistance = BigDecimal.ZERO;

        if (deliveries == null || deliveries.isEmpty()) {
            return totalDistance;
        }

        for (Delivery delivery : deliveries) {
            if (delivery.getPickupLatitude() != null && delivery.getPickupLongitude() != null
                    && delivery.getDeliveryLatitude() != null && delivery.getDeliveryLongitude() != null) {
                BigDecimal distance = distanceService.calculateRoadDistance(
                        delivery.getPickupLatitude(), delivery.getPickupLongitude(),
                        delivery.getDeliveryLatitude(), delivery.getDeliveryLongitude());
                totalDistance = totalDistance.add(distance);
                log.info("Delivery {} ({} -> {}): {} km",
                        delivery.getReference(), delivery.getPickupAddress(),
                        delivery.getDeliveryAddress(), distance);
            } else {
                log.warn("Delivery {} is missing GPS coordinates, skipping distance calculation",
                        delivery.getReference());
            }
        }

        return totalDistance;
    }
}
