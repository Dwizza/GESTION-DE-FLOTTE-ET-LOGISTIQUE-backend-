package com.fleet_management_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateClientRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

}
