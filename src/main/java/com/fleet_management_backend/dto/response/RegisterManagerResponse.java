package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterManagerResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;      // MANAGER
    private Boolean active;
}
