package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.TrailerRequest;
import com.fleet_management_backend.dto.response.TrailerResponse;
import com.fleet_management_backend.entity.Trailer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrailerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "tripTrailers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trailer toEntity(TrailerRequest request);

    TrailerResponse toResponse(Trailer trailer);
}
