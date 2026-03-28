package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.DeliveryRequest;
import com.fleet_management_backend.dto.response.DeliveryResponse;
import com.fleet_management_backend.entity.Delivery;
import com.fleet_management_backend.entity.DeliveryCategory;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.DeliveryMapper;
import com.fleet_management_backend.repository.DeliveryCategoryRepository;
import com.fleet_management_backend.repository.DeliveryRepository;
import com.fleet_management_backend.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fleet_management_backend.dto.response.PaginatedResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DeliveryService {

        private final DeliveryRepository deliveryRepository;
        private final TripRepository tripRepository;
        private final DeliveryCategoryRepository categoryRepository;
        private final DeliveryMapper deliveryMapper;

        @Transactional
        public DeliveryResponse createDelivery(DeliveryRequest request) {
                if (request.getReference() == null || request.getReference().trim().isEmpty()) {
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                        request.setReference("LIV-" + timestamp + "-" + random);
                } else if (deliveryRepository.existsByReference(request.getReference())) {
                        throw new ConflictException(
                                        "Delivery with reference " + request.getReference() + " already exists.");
                }

                Trip trip = tripRepository.findById(request.getTripId())
                                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

                validateTripCapacity(trip, request.getWeight(), request.getVolume(), null);

                Delivery delivery = deliveryMapper.toEntity(request);
                delivery.setTrip(trip);

                if (request.getCategoryId() != null) {
                        DeliveryCategory category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Delivery Category not found"));
                        delivery.setCategory(category);
                }

                Delivery savedDelivery = deliveryRepository.save(delivery);
                return deliveryMapper.toResponse(savedDelivery);
        }

        public List<DeliveryResponse> getAllDeliveries() {
                return deliveryRepository.findAll().stream()
                                .map(deliveryMapper::toResponse)
                                .collect(Collectors.toList());
        }

        public PaginatedResponse<DeliveryResponse> getPaginatedDeliveries(Pageable pageable) {
                Page<Delivery> deliveriesPage = deliveryRepository.findAll(pageable);
                return PaginatedResponse.<DeliveryResponse>builder()
                                .content(deliveriesPage.getContent().stream().map(deliveryMapper::toResponse).toList())
                                .pageNumber(deliveriesPage.getNumber())
                                .pageSize(deliveriesPage.getSize())
                                .totalElements(deliveriesPage.getTotalElements())
                                .totalPages(deliveriesPage.getTotalPages())
                                .last(deliveriesPage.isLast())
                                .build();
        }

        public DeliveryResponse getDeliveryById(UUID id) {
                Delivery delivery = deliveryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
                return deliveryMapper.toResponse(delivery);
        }

        @Transactional
        public DeliveryResponse updateDelivery(UUID id, DeliveryRequest request) {
                Delivery delivery = deliveryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

                if (request.getReference() != null && !request.getReference().trim().isEmpty()
                                && !delivery.getReference().equals(request.getReference())) {
                        if (deliveryRepository.existsByReference(request.getReference())) {
                                throw new ConflictException(
                                                "Delivery with reference " + request.getReference() + " already exists.");
                        }
                        delivery.setReference(request.getReference());
                }

                Trip trip = tripRepository.findById(request.getTripId())
                                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

                validateTripCapacity(trip, request.getWeight(), request.getVolume(), id);

                delivery.setWeight(request.getWeight());
                delivery.setVolume(request.getVolume());
                delivery.setPrix(request.getPrix());
                delivery.setPickupAddress(request.getPickupAddress());
                delivery.setPickupLatitude(request.getPickupLatitude());
                delivery.setPickupLongitude(request.getPickupLongitude());
                delivery.setDeliveryAddress(request.getDeliveryAddress());
                delivery.setDeliveryLatitude(request.getDeliveryLatitude());
                delivery.setDeliveryLongitude(request.getDeliveryLongitude());
                delivery.setStatus(request.getStatus());
                delivery.setTrip(trip);

                if (request.getCategoryId() != null) {
                        DeliveryCategory category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Delivery Category not found"));
                        delivery.setCategory(category);
                } else {
                        delivery.setCategory(null);
                }

                Delivery updatedDelivery = deliveryRepository.save(delivery);
                return deliveryMapper.toResponse(updatedDelivery);
        }

        @Transactional
        public void deleteDelivery(UUID id) {
                Delivery delivery = deliveryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
                deliveryRepository.delete(delivery);
        }

        private void validateTripCapacity(Trip trip, BigDecimal newWeight, BigDecimal newVolume,
                        UUID excludingDeliveryId) {
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
                                .map(tt -> tt.getTrailer().getMaxWeight() != null ? tt.getTrailer().getMaxWeight()
                                                : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal maxVolume = trip.getTripTrailers().stream()
                                .map(tt -> tt.getTrailer().getMaxVolume() != null ? tt.getTrailer().getMaxVolume()
                                                : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalWeight.compareTo(maxWeight) > 0) {
                        throw new ConflictException(
                                        "Total weight exceeds the maximum capacity of the assigned trailers ("
                                                        + maxWeight + ").");
                }
                if (totalVolume.compareTo(maxVolume) > 0) {
                        throw new ConflictException(
                                        "Total volume exceeds the maximum capacity of the assigned trailers ("
                                                        + maxVolume + ").");
                }
        }
}
