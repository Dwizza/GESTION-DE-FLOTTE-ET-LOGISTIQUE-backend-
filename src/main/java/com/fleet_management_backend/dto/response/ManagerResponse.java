package com.fleet_management_backend.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ManagerResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;
}
