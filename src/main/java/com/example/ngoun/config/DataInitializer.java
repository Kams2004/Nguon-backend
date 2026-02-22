package com.example.ngoun.config;

import com.example.ngoun.model.Role;
import com.example.ngoun.model.User;
import com.example.ngoun.repository.RoleRepository;
import com.example.ngoun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            adminRole.setCreatedAt(LocalDateTime.now());
            roleRepository.save(adminRole);
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@ngoun.com");
            adminUser.setRole(adminRole);
            adminUser.setActive(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(adminUser);
        }
    }
}
