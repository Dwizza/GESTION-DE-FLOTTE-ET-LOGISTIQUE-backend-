package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.CarburantTransactionRequest;
import com.fleet_management_backend.dto.response.CarburantTransactionResponse;
import com.fleet_management_backend.entity.CarburantTransaction;
import com.fleet_management_backend.entity.Truck;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.CarburantTransactionMapper;
import com.fleet_management_backend.repository.CarburantTransactionRepository;
import com.fleet_management_backend.repository.TruckRepository;
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

@Service
@RequiredArgsConstructor
public class CarburantService {

    private final CarburantTransactionRepository carburantRepository;
    private final TruckRepository truckRepository;
    private final CarburantTransactionMapper carburantMapper;

    @Transactional
    public CarburantTransactionResponse createTransaction(CarburantTransactionRequest request) {
        if (request.getReference() == null || request.getReference().trim().isEmpty()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            request.setReference("FL-" + timestamp + "-" + random);
        }

        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        CarburantTransaction transaction = carburantMapper.toEntity(request);
        transaction.setTruck(truck);

        CarburantTransaction saved = carburantRepository.save(transaction);
        return carburantMapper.toResponse(saved);
    }

    public List<CarburantTransactionResponse> getAllTransactions() {
        return carburantRepository.findAll().stream()
                .map(carburantMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<CarburantTransactionResponse> getPaginatedTransactions(Pageable pageable) {
        Page<CarburantTransaction> transactionsPage = carburantRepository.findAll(pageable);
        return PaginatedResponse.<CarburantTransactionResponse>builder()
                .content(transactionsPage.getContent().stream().map(carburantMapper::toResponse).toList())
                .pageNumber(transactionsPage.getNumber())
                .pageSize(transactionsPage.getSize())
                .totalElements(transactionsPage.getTotalElements())
                .totalPages(transactionsPage.getTotalPages())
                .last(transactionsPage.isLast())
                .build();
    }

    public CarburantTransactionResponse getTransactionById(UUID id) {
        CarburantTransaction transaction = carburantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return carburantMapper.toResponse(transaction);
    }

    @Transactional
    public CarburantTransactionResponse updateTransaction(UUID id, CarburantTransactionRequest request) {
        CarburantTransaction transaction = carburantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        transaction.setReference(request.getReference());
        transaction.setDateHeure(request.getDateHeure());
        transaction.setQuantite(request.getQuantite());
        transaction.setCout(request.getCout());
        transaction.setStationName(request.getStationName());
        transaction.setReceiptNumber(request.getReceiptNumber());
        transaction.setTruck(truck);

        CarburantTransaction updated = carburantRepository.save(transaction);
        return carburantMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        if (!carburantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        carburantRepository.deleteById(id);
    }
}
