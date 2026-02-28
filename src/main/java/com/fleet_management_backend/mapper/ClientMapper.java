package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.response.RegisterClientResponse;
import com.fleet_management_backend.entity.Client;
import com.fleet_management_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Client toEntity(RegisterClientRequest dto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.role", target = "role")
    @Mapping(source = "user.active", target = "active")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.address", target = "address")
    @Mapping(source = "client.companyName", target = "companyName")
    @Mapping(source = "client.phone", target = "phone")
    RegisterClientResponse toRegisterClientResponse(User user, Client client);
}
