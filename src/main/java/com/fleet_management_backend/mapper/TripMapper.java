package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.TripRequest;
import com.fleet_management_backend.dto.response.TripResponse;
import com.fleet_management_backend.entity.Trip;
import com.fleet_management_backend.entity.TripTrailer;
import com.fleet_management_backend.entity.TripTruck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalDistance", ignore = true) // Handled automatically via tracking later
    @Mapping(target = "createdByUserId", ignore = true) // Handled in Service via Security Context
    @Mapping(target = "driver", ignore = true) // Handled in Service
    @Mapping(target = "client", ignore = true) // Handled in Service
    @Mapping(target = "deliveries", ignore = true)
    @Mapping(target = "tripTrucks", ignore = true) // Handled in Service
    @Mapping(target = "tripTrailers", ignore = true) // Handled in Service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(TripRequest request);

    @Mapping(target = "driver.id", source = "driver.id")
    @Mapping(target = "driver.licenseNumber", source = "driver.licenseNumber")
    @Mapping(target = "driver.firstName", source = "driver.user.firstName")
    @Mapping(target = "driver.lastName", source = "driver.user.lastName")
    @Mapping(target = "client.id", source = "client.id")
    @Mapping(target = "client.companyName", source = "client.companyName")
    @Mapping(target = "trucks", source = "tripTrucks", qualifiedByName = "mapTrucks")
    @Mapping(target = "trailers", source = "tripTrailers", qualifiedByName = "mapTrailers")
    TripResponse toResponse(Trip trip);

    @Named("mapTrucks")
    default List<TripResponse.SimpleTruckResponse> mapTrucks(List<TripTruck> tripTrucks) {
        if (tripTrucks == null)
            return Collections.emptyList();
        return tripTrucks.stream()
                .map(tt -> TripResponse.SimpleTruckResponse.builder()
                        .id(tt.getTruck().getId())
                        .registrationNumber(tt.getTruck().getRegistrationNumber())
                        .brand(tt.getTruck().getBrand())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("mapTrailers")
    default List<TripResponse.SimpleTrailerResponse> mapTrailers(List<TripTrailer> tripTrailers) {
        if (tripTrailers == null)
            return Collections.emptyList();
        return tripTrailers.stream()
                .map(tt -> TripResponse.SimpleTrailerResponse.builder()
                        .id(tt.getTrailer().getId())
                        .type(tt.getTrailer().getType().name())
                        .build())
                .collect(Collectors.toList());
    }
}
