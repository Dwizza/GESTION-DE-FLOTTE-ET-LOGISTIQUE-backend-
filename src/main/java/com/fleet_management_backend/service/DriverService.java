package com.fleet_management_backend.service;

import com.fleet_management_backend.dto.request.RegisterDriverRequest;
import com.fleet_management_backend.dto.request.UpdateDriverRequest;
import com.fleet_management_backend.dto.response.DriverResponse;
import com.fleet_management_backend.dto.response.RegisterDriverResponse;
import com.fleet_management_backend.entity.Driver;
import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.mapper.DriverMapper;
import com.fleet_management_backend.mapper.UserMapper;
import com.fleet_management_backend.repository.DriverRepository;
import com.fleet_management_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DriverService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DriverMapper driverMapper;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public RegisterDriverResponse createDriver(RegisterDriverRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = userMapper.toEntity(req);
        user.setRole(Role.DRIVER);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user = userRepository.save(user);

        Driver driver = driverMapper.toEntity(req);
        driver.setUser(user);
        driver.setAvailable(true);
        driver.setLicenseNumber(req.getLicenseNumber());
        driver.setPhoneNumber(req.getPhoneNumber());
        driver = driverRepository.save(driver);

        return driverMapper.toRegisterDriverResponse(user, driver);
    }

    @Transactional
    public void deleteDriver(UUID driverId) {
        userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driverRepository.delete(driver);
        userRepository.delete(driver.getUser());
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(driverMapper::toDriverResponse)
                .toList();
    }

    public DriverResponse getDriverById(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        return driverMapper.toDriverResponse(driver);
    }

    @Transactional
    public DriverResponse updateDriver(UUID driverId, UpdateDriverRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        User user = driver.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvailable() != null) {
            driver.setAvailable(request.getAvailable());
        }

        userRepository.save(user);
        driver = driverRepository.save(driver);

        return driverMapper.toDriverResponse(driver);
    }
}
