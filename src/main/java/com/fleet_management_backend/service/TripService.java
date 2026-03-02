package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TripRequest;
import com.fleet_management_backend.dto.response.TripResponse;
import com.fleet_management_backend.entity.*;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.TripMapper;
import com.fleet_management_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final ClientRepository clientRepository;
    private final TruckRepository truckRepository;
    private final TrailerRepository trailerRepository;
    private final TripMapper tripMapper;

    @Transactional
    public TripResponse createTrip(TripRequest request, UUID createdByUserId) {
        if (tripRepository.existsByReference(request.getReference())) {
            throw new ConflictException("Trip with reference " + request.getReference() + " already exists.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (!driver.getAvailable()) {
            throw new ConflictException("Driver is currently not available for a new trip.");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setClient(client);
        trip.setDriver(driver);
        trip.setCreatedByUserId(createdByUserId);
        trip.setTotalDistance(BigDecimal.ZERO);

        if (request.getTruckIds() != null && !request.getTruckIds().isEmpty()) {
            for (UUID truckId : request.getTruckIds()) {
                Truck truck = truckRepository.findById(truckId)
                        .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));
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

    @Transactional
    public TripResponse updateTrip(UUID id, TripRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getReference().equals(request.getReference()) &&
                tripRepository.existsByReference(request.getReference())) {
            throw new ConflictException("Trip with reference " + request.getReference() + " already exists.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        trip.setReference(request.getReference());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setStatus(request.getStatus());
        trip.setClient(client);
        trip.setDriver(driver);

        trip.getTripTrucks().clear();
        if (request.getTruckIds() != null && !request.getTruckIds().isEmpty()) {
            for (UUID truckId : request.getTruckIds()) {
                Truck truck = truckRepository.findById(truckId)
                        .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));
                TripTruck tripTruck = new TripTruck();
                tripTruck.setTrip(trip);
                tripTruck.setTruck(truck);
                tripTruck.setReference(request.getReference() + "-TRK-" + truck.getRegistrationNumber());
                trip.getTripTrucks().add(tripTruck);
            }
        }

        trip.getTripTrailers().clear();
        if (request.getTrailerIds() != null && !request.getTrailerIds().isEmpty()) {
            for (UUID trailerId : request.getTrailerIds()) {
                Trailer trailer = trailerRepository.findById(trailerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Trailer not found"));
                TripTrailer tripTrailer = new TripTrailer();
                tripTrailer.setTrip(trip);
                tripTrailer.setTrailer(trailer);
                tripTrailer.setReference(request.getReference() + "-TRL-" + trailerId.toString().substring(0, 4));
                trip.getTripTrailers().add(tripTrailer);
            }
        }

        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(updatedTrip);
    }

    @Transactional
    public void deleteTrip(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        tripRepository.delete(trip);
    }
}
