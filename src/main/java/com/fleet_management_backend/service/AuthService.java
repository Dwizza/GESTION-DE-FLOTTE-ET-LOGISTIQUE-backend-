package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.LoginRequest;
import com.fleet_management_backend.dto.request.RegisterClientRequest;
import com.fleet_management_backend.dto.response.LoginResponse;
import com.fleet_management_backend.dto.response.RegisterClientResponse;
import com.fleet_management_backend.entity.Client;
import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;
import com.fleet_management_backend.exception.BadRequestException;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.mapper.ClientMapper;
import com.fleet_management_backend.mapper.UserMapper;
import com.fleet_management_backend.repository.ClientRepository;
import com.fleet_management_backend.repository.UserRepository;
import com.fleet_management_backend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserMapper userMapper;
    private final ClientMapper clientMapper;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public RegisterClientResponse registerClient(RegisterClientRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = userMapper.toEntity(req);
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user = userRepository.save(user);

        Client client = clientMapper.toEntity(req);
        client.setUser(user);
        client = clientRepository.save(client);

        user.setClient(client);
        return clientMapper.toRegisterClientResponse(user, client);
    }
}
