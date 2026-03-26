package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.MaintenanceRequest;
import com.fleet_management_backend.dto.response.MaintenanceResponse;
import com.fleet_management_backend.entity.Maintenance;
import com.fleet_management_backend.entity.Trailer;
import com.fleet_management_backend.entity.Truck;
import com.fleet_management_backend.entity.enums.TruckStatus;
import com.fleet_management_backend.entity.enums.TrailerStatus;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.MaintenanceMapper;
import com.fleet_management_backend.repository.MaintenanceRepository;
import com.fleet_management_backend.repository.TrailerRepository;
import com.fleet_management_backend.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fleet_management_backend.dto.response.PaginatedResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final TruckRepository truckRepository;
    private final TrailerRepository trailerRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Transactional
    public MaintenanceResponse createMaintenance(MaintenanceRequest request) {
        Maintenance maintenance = maintenanceMapper.toEntity(request);

        if (request.getTruckId() != null) {
            Truck truck = truckRepository.findById(request.getTruckId())
                    .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

            if (truck.getStatus() == TruckStatus.IN_TRIP) {
                throw new com.fleet_management_backend.exception.ConflictException(
                        "Cannot perform maintenance on a truck that is currently in a trip.");
            }

            maintenance.setTruck(truck);

            // Logique métier: Changement d'état du camion vers "Maintenance" durant
            // l'intervention
            if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.IN_PROGRESS
                    || request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.PLANNED) {
                truck.setStatus(TruckStatus.IN_MAINTENANCE);
                truckRepository.save(truck);
            } else if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.COMPLETED) {
                truck.setStatus(TruckStatus.AVAILABLE);
                truckRepository.save(truck);
            }
        }

        if (request.getTrailerId() != null) {
            Trailer trailer = trailerRepository.findById(request.getTrailerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trailer not found"));

            if (trailer.getStatus() == TrailerStatus.IN_USE) {
                throw new com.fleet_management_backend.exception.ConflictException(
                        "Cannot perform maintenance on a trailer that is currently in use.");
            }

            maintenance.setTrailer(trailer);

            if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.IN_PROGRESS
                    || request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.PLANNED) {
                trailer.setStatus(TrailerStatus.IN_MAINTENANCE);
                trailerRepository.save(trailer);
            } else if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.COMPLETED) {
                trailer.setStatus(TrailerStatus.AVAILABLE);
                trailerRepository.save(trailer);
            }
        }

        Maintenance saved = maintenanceRepository.save(maintenance);
        return maintenanceMapper.toResponse(saved);
    }

    public List<MaintenanceResponse> getAllMaintenances() {
        return maintenanceRepository.findAll().stream()
                .map(maintenanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<MaintenanceResponse> getPaginatedMaintenances(Pageable pageable) {
        Page<Maintenance> maintenancePage = maintenanceRepository.findAll(pageable);
        return PaginatedResponse.<MaintenanceResponse>builder()
                .content(maintenancePage.getContent().stream().map(maintenanceMapper::toResponse).toList())
                .pageNumber(maintenancePage.getNumber())
                .pageSize(maintenancePage.getSize())
                .totalElements(maintenancePage.getTotalElements())
                .totalPages(maintenancePage.getTotalPages())
                .last(maintenancePage.isLast())
                .build();
    }

    @Transactional
    public MaintenanceResponse updateMaintenance(UUID id, MaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

        maintenance.setType(request.getType());
        maintenance.setDateMaintenance(request.getDateMaintenance());
        maintenance.setCout(request.getCout());
        maintenance.setReference(request.getReference());
        maintenance.setDescription(request.getDescription());
        maintenance.setPerformedBy(request.getPerformedBy());

        // Handle Status Change
        maintenance.setStatus(request.getStatus());

        if (maintenance.getTruck() != null) {
            Truck truck = maintenance.getTruck();
            if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.IN_PROGRESS
                    || request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.PLANNED) {
                truck.setStatus(TruckStatus.IN_MAINTENANCE);
            } else if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.COMPLETED) {
                truck.setStatus(TruckStatus.AVAILABLE);
            }
            truckRepository.save(truck);
        }

        if (maintenance.getTrailer() != null) {
            Trailer trailer = maintenance.getTrailer();
            if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.IN_PROGRESS
                    || request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.PLANNED) {
                trailer.setStatus(TrailerStatus.IN_MAINTENANCE);
            } else if (request.getStatus() == com.fleet_management_backend.entity.enums.MaintenanceStatus.COMPLETED) {
                trailer.setStatus(TrailerStatus.AVAILABLE);
            }
            trailerRepository.save(trailer);
        }

        Maintenance updated = maintenanceRepository.save(maintenance);
        return maintenanceMapper.toResponse(updated);
    }

    public MaintenanceResponse getMaintenanceById(UUID id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));
        return maintenanceMapper.toResponse(maintenance);
    }

    @Transactional
    public void deleteMaintenance(UUID id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found"));

        if (maintenance.getTruck() != null) {
            Truck truck = maintenance.getTruck();
            truck.setStatus(TruckStatus.AVAILABLE);
            truckRepository.save(truck);
        }

        if (maintenance.getTrailer() != null) {
            Trailer trailer = maintenance.getTrailer();
            trailer.setStatus(TrailerStatus.AVAILABLE);
            trailerRepository.save(trailer);
        }

        maintenanceRepository.delete(maintenance);
    }
}
