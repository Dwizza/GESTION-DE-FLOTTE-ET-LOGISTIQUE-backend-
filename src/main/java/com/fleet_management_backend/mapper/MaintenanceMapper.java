package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.MaintenanceRequest;
import com.fleet_management_backend.dto.response.MaintenanceResponse;
import com.fleet_management_backend.entity.Maintenance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "truck", ignore = true)
    @Mapping(target = "trailer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Maintenance toEntity(MaintenanceRequest request);

    @Mapping(source = "truck.id", target = "truckId")
    @Mapping(source = "trailer.id", target = "trailerId")
    MaintenanceResponse toResponse(Maintenance maintenance);
}
