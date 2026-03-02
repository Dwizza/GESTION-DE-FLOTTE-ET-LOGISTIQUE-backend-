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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final TripRepository tripRepository;
    private final DeliveryCategoryRepository categoryRepository;
    private final DeliveryMapper deliveryMapper;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        if (deliveryRepository.existsByReference(request.getReference())) {
            throw new ConflictException("Delivery with reference " + request.getReference() + " already exists.");
        }

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        Delivery delivery = deliveryMapper.toEntity(request);
        delivery.setTrip(trip);

        if (request.getCategoryId() != null) {
            DeliveryCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Delivery Category not found"));
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

    public DeliveryResponse getDeliveryById(UUID id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return deliveryMapper.toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateDelivery(UUID id, DeliveryRequest request) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (!delivery.getReference().equals(request.getReference()) &&
                deliveryRepository.existsByReference(request.getReference())) {
            throw new ConflictException("Delivery with reference " + request.getReference() + " already exists.");
        }

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        delivery.setReference(request.getReference());
        delivery.setWeight(request.getWeight());
        delivery.setVolume(request.getVolume());
        delivery.setPickupAddress(request.getPickupAddress());
        delivery.setDeliveryAddress(request.getDeliveryAddress());
        delivery.setStatus(request.getStatus());
        delivery.setTrip(trip);

        if (request.getCategoryId() != null) {
            DeliveryCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Delivery Category not found"));
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
}
