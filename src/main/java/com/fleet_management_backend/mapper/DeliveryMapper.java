package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.DeliveryRequest;
import com.fleet_management_backend.dto.response.DeliveryResponse;
import com.fleet_management_backend.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true) // Handled in Service
    @Mapping(target = "category", ignore = true) // Handled in Service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Delivery toEntity(DeliveryRequest request);

    @Mapping(target = "trip.id", source = "trip.id")
    @Mapping(target = "trip.reference", source = "trip.reference")
    @Mapping(target = "trip.status", source = "trip.status")
    @Mapping(target = "category.id", source = "category.id")
    @Mapping(target = "category.name", source = "category.name")
    DeliveryResponse toResponse(Delivery delivery);
}
