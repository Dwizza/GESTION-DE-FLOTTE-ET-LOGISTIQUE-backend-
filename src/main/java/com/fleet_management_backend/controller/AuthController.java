package com.fleet_management_backend.controller;

import com.fleet_management_backend.dto.request.LoginRequest;
import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.response.LoginResponse;
import com.fleet_management_backend.dto.response.RegisterClientResponse;
import com.fleet_management_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/client")
    public ResponseEntity<RegisterClientResponse> registerClient(@Valid @RequestBody RegisterClientRequest request) {
        return ResponseEntity.ok(authService.registerClient(request));
    }

}
