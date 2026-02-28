package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterDriverResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Boolean active;

    private UUID driverId;
    private String licenseNumber;
    private String phoneNumber;
}
