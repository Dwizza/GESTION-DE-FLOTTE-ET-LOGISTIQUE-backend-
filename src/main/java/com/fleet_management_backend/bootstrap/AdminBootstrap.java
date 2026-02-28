package com.fleet_management_backend.bootstrap;

import com.fleet_management_backend.entity.User;
import com.fleet_management_backend.entity.enums.Role;
import com.fleet_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.enabled}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled) return;

        if (userRepository.existsByEmail(adminEmail)) return;

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("System");
        admin.setEmail(adminEmail);
        admin.setActive(true);
        admin.setRole(Role.ADMIN);

        admin.setPasswordHash(passwordEncoder.encode(adminPassword));

        userRepository.save(admin);

        System.out.println("✅ Admin seeded: " + adminEmail);
    }
}
