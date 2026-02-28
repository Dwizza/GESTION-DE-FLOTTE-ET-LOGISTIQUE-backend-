package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.TrailerRequest;
import com.fleet_management_backend.dto.response.TrailerResponse;
import com.fleet_management_backend.entity.Trailer;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import com.fleet_management_backend.mapper.TrailerMapper;
import com.fleet_management_backend.repository.TrailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrailerService {

    private final TrailerRepository trailerRepository;
    private final TrailerMapper trailerMapper;

    @Transactional
    public TrailerResponse createTrailer(TrailerRequest request) {
        Trailer trailer = trailerMapper.toEntity(request);
        Trailer savedTrailer = trailerRepository.save(trailer);
        return trailerMapper.toResponse(savedTrailer);
    }

    public List<TrailerResponse> getAllTrailers() {
        return trailerRepository.findAll().stream()
                .map(trailerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TrailerResponse getTrailerById(UUID id) {
        Trailer trailer = findTrailerById(id);
        return trailerMapper.toResponse(trailer);
    }

    @Transactional
    public TrailerResponse updateTrailer(UUID id, TrailerRequest request) {
        Trailer trailer = findTrailerById(id);

        trailer.setType(request.getType());
        trailer.setMaxWeight(request.getMaxWeight());
        trailer.setMaxVolume(request.getMaxVolume());
        trailer.setStatus(request.getStatus());

        Trailer updatedTrailer = trailerRepository.save(trailer);
        return trailerMapper.toResponse(updatedTrailer);
    }

    @Transactional
    public void deleteTrailer(UUID id) {
        Trailer trailer = findTrailerById(id);
        trailerRepository.delete(trailer);
    }

    private Trailer findTrailerById(UUID id) {
        return trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + id));
    }
}
