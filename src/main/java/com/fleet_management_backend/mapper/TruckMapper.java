package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.TruckRequest;
import com.fleet_management_backend.dto.response.TruckResponse;
import com.fleet_management_backend.entity.Truck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TruckMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carburantTransactions", ignore = true)
    @Mapping(target = "trackingPoints", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "tripTrucks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Truck toEntity(TruckRequest request);

    TruckResponse toResponse(Truck truck);
}
