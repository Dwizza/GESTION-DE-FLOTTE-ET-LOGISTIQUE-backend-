package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.response.DriverResponse;
import com.fleet_management_backend.dto.response.RegisterDriverResponse;
import com.fleet_management_backend.entity.Driver;
import com.fleet_management_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Driver toEntity(RegisterDriverRequest dto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.role", target = "role")
    @Mapping(source = "user.active", target = "active")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.licenseNumber", target = "licenseNumber")
    @Mapping(source = "driver.phoneNumber", target = "phoneNumber")
    RegisterDriverResponse toRegisterDriverResponse(User user, Driver driver);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.active", target = "active")
    @Mapping(source = "licenseNumber", target = "licenseNumber")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "available", target = "available")
    DriverResponse toDriverResponse(Driver driver);
}
