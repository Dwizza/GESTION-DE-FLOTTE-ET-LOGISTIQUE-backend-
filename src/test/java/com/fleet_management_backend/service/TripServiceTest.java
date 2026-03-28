package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TripRequest;
import com.fleet_management_backend.dto.response.TripResponse;
import com.fleet_management_backend.entity.Client;
import com.fleet_management_backend.entity.Delivery;
import com.fleet_management_backend.entity.Driver;
import com.fleet_management_backend.entity.Trailer;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.entity.enums.TrailerType;
import com.fleet_management_backend.entity.enums.TripStatus;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.mapper.TripMapper;
import com.fleet_management_backend.repository.ClientRepository;
import com.fleet_management_backend.repository.DeliveryRepository;
import com.fleet_management_backend.repository.DriverRepository;
import com.fleet_management_backend.repository.MaintenanceRepository;
import com.fleet_management_backend.repository.TrailerRepository;
import com.fleet_management_backend.repository.TripRepository;
import com.fleet_management_backend.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TruckRepository truckRepository;
    @Mock
    private TrailerRepository trailerRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private MaintenanceRepository maintenanceRepository;
    @Mock
    private TripMapper tripMapper;
    @Mock
    private DistanceService distanceService;
    @Mock
    private MaintenanceService maintenanceService;

    private TripService tripService;

    private Trip existingTrip;
    private Driver driver;
    private Client client;

    @BeforeEach
    void setUp() {
        tripService = new TripService(
                tripRepository,
                driverRepository,
                clientRepository,
                truckRepository,
                trailerRepository,
                deliveryRepository,
                maintenanceRepository,
                tripMapper,
                distanceService,
                new CapacityValidationService(),
                maintenanceService);

        driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setAvailable(true);

        client = new Client();
        client.setId(UUID.randomUUID());

        existingTrip = new Trip();
        existingTrip.setId(UUID.randomUUID());
        existingTrip.setReference("TRIP-001");
        existingTrip.setDriver(driver);
        existingTrip.setClient(client);
        existingTrip.setStatus(TripStatus.PLANNED);
        existingTrip.setStartDate(LocalDate.now());
        existingTrip.setDeliveries(new ArrayList<>());
        existingTrip.setTripTrucks(new ArrayList<>());
        existingTrip.setTripTrailers(new ArrayList<>());
    }

    private TripRequest baseUpdateRequest(UUID trailerId) {
        TripRequest request = new TripRequest();
        request.setReference("TRIP-001");
        request.setStartDate(existingTrip.getStartDate());
        request.setDriverId(driver.getId());
        request.setClientId(client.getId());
        request.setStatus(TripStatus.PLANNED);
        request.setTrailerIds(List.of(trailerId));
        return request;
    }

    @Test
    void updateTrip_shouldThrowConflict_whenExistingDeliveriesExceedNewTrailersCapacity() {
        UUID tripId = existingTrip.getId();
        UUID newTrailerId = UUID.randomUUID();
        TripRequest request = baseUpdateRequest(newTrailerId);

        Delivery heavyDelivery = new Delivery();
        heavyDelivery.setWeight(new BigDecimal("5000"));
        heavyDelivery.setVolume(new BigDecimal("20"));
        existingTrip.getDeliveries().add(heavyDelivery);

        Trailer weakTrailer = new Trailer();
        weakTrailer.setId(newTrailerId);
        weakTrailer.setType(TrailerType.STANDARD);
        weakTrailer.setStatus(TrailerStatus.AVAILABLE);
        weakTrailer.setMaxWeight(new BigDecimal("3000"));
        weakTrailer.setMaxVolume(new BigDecimal("50"));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(trailerRepository.findById(newTrailerId)).thenReturn(Optional.of(weakTrailer));

        assertThrows(ConflictException.class, () -> tripService.updateTrip(tripId, request));

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldSucceed_whenDeliveriesFitNewTrailersCapacity() {
        UUID tripId = existingTrip.getId();
        UUID newTrailerId = UUID.randomUUID();
        TripRequest request = baseUpdateRequest(newTrailerId);

        Delivery normalDelivery = new Delivery();
        normalDelivery.setWeight(new BigDecimal("2000"));
        normalDelivery.setVolume(new BigDecimal("10"));
        existingTrip.getDeliveries().add(normalDelivery);

        Trailer strongTrailer = new Trailer();
        strongTrailer.setId(newTrailerId);
        strongTrailer.setType(TrailerType.STANDARD);
        strongTrailer.setStatus(TrailerStatus.AVAILABLE);
        strongTrailer.setMaxWeight(new BigDecimal("5000"));
        strongTrailer.setMaxVolume(new BigDecimal("30"));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(trailerRepository.findById(newTrailerId)).thenReturn(Optional.of(strongTrailer));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        TripResponse expected = TripResponse.builder()
                .id(existingTrip.getId())
                .reference(existingTrip.getReference())
                .status(TripStatus.PLANNED)
                .build();
        when(tripMapper.toResponse(any(Trip.class))).thenReturn(expected);

        TripResponse result = tripService.updateTrip(tripId, request);

        assertNotNull(result);
        assertEquals("TRIP-001", result.getReference());
        verify(tripRepository).save(any(Trip.class));
        verify(tripMapper).toResponse(any(Trip.class));
    }
}
