package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TruckRequest;
import com.fleet_management_backend.dto.response.TruckResponse;
import com.fleet_management_backend.entity.Truck;
import com.fleet_management_backend.mapper.TruckMapper;
import com.fleet_management_backend.repository.TruckRepository;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fleet_management_backend.dto.response.PaginatedResponse;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository truckRepository;
    private final TruckMapper truckMapper;

    @Transactional
    public TruckResponse createTruck(TruckRequest request) {
        if (truckRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new ConflictException("Truck with this registration number already exists");
        }
        Truck truck = truckMapper.toEntity(request);
        Truck savedTruck = truckRepository.save(truck);
        return truckMapper.toResponse(savedTruck);
    }

    public List<TruckResponse> getAllTrucks() {
        return truckRepository.findAll().stream()
                .map(truckMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TruckResponse getTruckById(UUID id) {
        Truck truck = findTruckById(id);
        return truckMapper.toResponse(truck);
    }

    @Transactional
    public TruckResponse updateTruck(UUID id, TruckRequest request) {
        Truck truck = findTruckById(id);

        if (!truck.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
                truckRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new ConflictException("Truck with this registration number already exists");
        }

        truck.setRegistrationNumber(request.getRegistrationNumber());
        truck.setBrand(request.getBrand());
        truck.setStatus(request.getStatus());
        truck.setTotalMileage(request.getTotalMileage());

        Truck updatedTruck = truckRepository.save(truck);
        return truckMapper.toResponse(updatedTruck);
    }

    @Transactional
    public void deleteTruck(UUID id) {
        Truck truck = findTruckById(id);
        truckRepository.delete(truck);
    }

    public PaginatedResponse<TruckResponse> getPaginatedTrucks(Pageable pageable) {
        Page<Truck> trucksPage = truckRepository.findAll(pageable);
        return PaginatedResponse.<TruckResponse>builder()
                .content(trucksPage.getContent().stream().map(truckMapper::toResponse).toList())
                .pageNumber(trucksPage.getNumber())
                .pageSize(trucksPage.getSize())
                .totalElements(trucksPage.getTotalElements())
                .totalPages(trucksPage.getTotalPages())
                .last(trucksPage.isLast())
                .build();
    }

    private Truck findTruckById(UUID id) {
        return truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found with id: " + id));
    }
}
