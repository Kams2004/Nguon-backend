package com.example.ngoun.config;

import com.example.ngoun.model.Role;
import com.example.ngoun.model.ShopCategory;
import com.example.ngoun.model.User;
import com.example.ngoun.repository.RoleRepository;
import com.example.ngoun.repository.ShopCategoryRepository;
import com.example.ngoun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShopCategoryRepository shopCategoryRepository;

    @Override
    public void run(String... args) {
        try {
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                log.info("Creating default admin role...");
                Role role = new Role();
                role.setName("ADMIN");
                role.setDescription("Administrator role");
                role.setCreatedAt(LocalDateTime.now());
                return roleRepository.save(role);
            });

            // Each seed account is created independently — on a database
            // that already has the ADMIN role (e.g. production), a newly
            // added account here still gets created on the next boot.
            seedAdminUser(adminRole, "admin", "admin123", "admin@ngoun.com");
            seedAdminUser(adminRole, "Nguon", "Nguon2026", "nguon@ngoun.com");
        } catch (Exception e) {
            log.error("Error initializing default data: {}", e.getMessage(), e);
        }

        seedShopCategories();
    }

    private void seedAdminUser(Role adminRole, String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username).isPresent()) return;
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setRole(adminRole);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Admin user '{}' created successfully", username);
    }

    private void seedShopCategories() {
        try {
            if (shopCategoryRepository.count() > 0) {
                log.info("Shop categories already exist, skipping initialization");
                return;
            }
            log.info("Creating default shop categories...");
            record Seed(String key, String label, String icon, int order) {}
            List<Seed> seeds = List.of(
                    new Seed("artisanat", "Artisanat", "palette", 0),
                    new Seed("vetements", "Vêtements & Textiles", "shirt", 1),
                    new Seed("gastronomie", "Gastronomie", "utensils", 2),
                    new Seed("livres", "Livres & Culture", "book-open", 3),
                    new Seed("bijoux", "Bijoux & Parures", "gem", 4),
                    new Seed("decoration", "Décoration", "wand-sparkles", 5)
            );
            for (Seed s : seeds) {
                ShopCategory c = new ShopCategory();
                c.setKey(s.key());
                c.setLabel(s.label());
                c.setIcon(s.icon());
                c.setDisplayOrder(s.order());
                shopCategoryRepository.save(c);
            }
            log.info("Shop categories created successfully");
        } catch (Exception e) {
            log.error("Error initializing shop categories: {}", e.getMessage(), e);
        }
    }
}
