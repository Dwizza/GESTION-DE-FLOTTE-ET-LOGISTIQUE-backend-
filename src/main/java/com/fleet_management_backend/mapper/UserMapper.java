package com.fleet_management_backend.mapper;

import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.request.RegisterManagerRequest;
import com.fleet_management_backend.dto.response.ManagerResponse;
import com.fleet_management_backend.dto.response.RegisterManagerResponse;
import com.fleet_management_backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "client", ignore = true)
    User toEntity(RegisterManagerRequest dto);

    @InheritConfiguration(name = "toEntity")
    User toEntity(RegisterDriverRequest dto);

    @InheritConfiguration(name = "toEntity")
    User toEntity(RegisterClientRequest dto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "active", target = "active")
    RegisterManagerResponse toRegisterManagerDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateEntity(RegisterManagerRequest dto, @MappingTarget User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "active", target = "active")
    ManagerResponse toManagerResponse(User user);
}
