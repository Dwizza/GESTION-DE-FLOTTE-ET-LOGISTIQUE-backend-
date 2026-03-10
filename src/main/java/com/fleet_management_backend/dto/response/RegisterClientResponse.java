package com.fleet_management_backend.dto.response;

import com.fleet_management_backend.entity.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterClientResponse {

    private UUID userId;
    private String email;
    private Role role;
    private Boolean active;

    private UUID clientId;
    private String address;
    private String companyName;
    private String phone;
}
