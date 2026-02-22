package com.example.ngoun.config;

import com.example.ngoun.model.Role;
import com.example.ngoun.model.User;
import com.example.ngoun.repository.RoleRepository;
import com.example.ngoun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                log.info("Creating default admin role and user...");
                
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setDescription("Administrator role");
                adminRole.setCreatedAt(LocalDateTime.now());
                adminRole = roleRepository.save(adminRole);
                log.info("Admin role created with ID: {}", adminRole.getId());
                
                if (userRepository.findByUsername("admin").isEmpty()) {
                    User adminUser = new User();
                    adminUser.setUsername("admin");
                    adminUser.setPassword(passwordEncoder.encode("admin123"));
                    adminUser.setEmail("admin@ngoun.com");
                    adminUser.setRole(adminRole);
                    adminUser.setActive(true);
                    adminUser.setCreatedAt(LocalDateTime.now());
                    userRepository.save(adminUser);
                    log.info("Admin user created successfully");
                }
            } else {
                log.info("Admin role already exists, skipping initialization");
            }
        } catch (Exception e) {
            log.error("Error initializing default data: {}", e.getMessage(), e);
        }
    }
}
