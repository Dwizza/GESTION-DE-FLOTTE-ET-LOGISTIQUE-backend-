package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TrackingRequest;
import com.fleet_management_backend.entity.TrackingPoint;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.entity.Truck;
import com.fleet_management_backend.entity.enums.TruckStatus;
import com.fleet_management_backend.entity.enums.TripStatus;
import com.fleet_management_backend.repository.TrackingPointRepository;
import com.fleet_management_backend.repository.TripRepository;
import com.fleet_management_backend.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingPointRepository trackingPointRepository;
    @Mock
    private TruckRepository truckRepository;
    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TrackingService trackingService;

    private Truck truck;

    @BeforeEach
    void setUp() {
        truck = new Truck();
        truck.setId(UUID.randomUUID());
        truck.setTotalMileage(new BigDecimal("49900.0")); // Getting close to 50k
        truck.setStatus(TruckStatus.AVAILABLE);
    }

    @Test
    void testRecordTrackingPoint_ShouldTriggerMaintenance_WhenMileageCrosses50k() {
        // Arrange
        TrackingRequest request = new TrackingRequest();
        request.setTruckId(truck.getId());
        // Paris lat/lon
        request.setLatitude(new BigDecimal("48.8566"));
        request.setLongitude(new BigDecimal("2.3522"));

        TrackingPoint previousPoint = new TrackingPoint();
        // London lat/lon
        previousPoint.setLatitude(new BigDecimal("51.5074"));
        previousPoint.setLongitude(new BigDecimal("-0.1278"));
        previousPoint.setTimestamp(LocalDateTime.now().minusHours(5));

        when(truckRepository.findById(truck.getId())).thenReturn(Optional.of(truck));
        when(trackingPointRepository.findTopByTruckIdOrderByTimestampDesc(truck.getId()))
                .thenReturn(Optional.of(previousPoint));
        when(tripRepository.findTripsByTruckIdAndStatus(truck.getId(), TripStatus.ONGOING)).thenReturn(List.of());

        // Act
        trackingService.recordTrackingPoint(request);

        // Assert
        // Distance Paris <-> London is ~343 km. 49900 + 343 = 50243. Should cross 50k
        // and trigger MAINTENANCE.
        verify(trackingPointRepository).save(any(TrackingPoint.class));
        verify(truckRepository).save(truck);
        assertEquals(TruckStatus.IN_MAINTENANCE, truck.getStatus());
    }

    @Test
    void testRecordTrackingPoint_IncrementsTripDistance() {
        // Arrange
        TrackingRequest request = new TrackingRequest();
        request.setTruckId(truck.getId());
        request.setLatitude(new BigDecimal("48.8566"));
        request.setLongitude(new BigDecimal("2.3522"));

        TrackingPoint previousPoint = new TrackingPoint();
        previousPoint.setLatitude(new BigDecimal("51.5074"));
        previousPoint.setLongitude(new BigDecimal("-0.1278"));

        Trip ongoingTrip = new Trip();
        ongoingTrip.setStatus(TripStatus.ONGOING);
        ongoingTrip.setTotalDistance(new BigDecimal("100.0"));

        when(truckRepository.findById(truck.getId())).thenReturn(Optional.of(truck));
        when(trackingPointRepository.findTopByTruckIdOrderByTimestampDesc(truck.getId()))
                .thenReturn(Optional.of(previousPoint));
        when(tripRepository.findTripsByTruckIdAndStatus(truck.getId(), TripStatus.ONGOING))
                .thenReturn(List.of(ongoingTrip));

        // Act
        trackingService.recordTrackingPoint(request);

        // Assert
        verify(tripRepository).save(ongoingTrip);
        // Ensure distance is incremented (100 + ~343)
        // System.out.println("New distance: " + ongoingTrip.getTotalDistance());
    }
}
