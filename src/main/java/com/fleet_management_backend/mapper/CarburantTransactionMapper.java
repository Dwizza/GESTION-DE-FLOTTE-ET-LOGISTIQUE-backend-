package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.CarburantTransactionRequest;
import com.fleet_management_backend.dto.response.CarburantTransactionResponse;
import com.fleet_management_backend.entity.CarburantTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarburantTransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "truck", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CarburantTransaction toEntity(CarburantTransactionRequest request);

    @Mapping(source = "truck.id", target = "truckId")
    CarburantTransactionResponse toResponse(CarburantTransaction transaction);
}
