package com.example.healthcare.config;

import com.example.healthcare.model.enums.Role;
import com.example.healthcare.model.sql.Admin;
import com.example.healthcare.model.sql.User;
import com.example.healthcare.repository.sql.AdminRepository;
import com.example.healthcare.repository.sql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (userRepository.findByRole(Role.ADMIN).isEmpty()) {

                User savedUser = userRepository.save(User.builder()
                        .username("admin")
                        .email("admin@healthcare.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build());

                adminRepository.save(Admin.builder()
                        .user(savedUser)
                        .name("System Admin")
                        .department("IT")
                        .build());

                log.info("========================================");
                log.info("Default admin created:");
                log.info("Username: admin");
                log.info("Password: admin123");
                log.info("========================================");
            } else {
                log.info("Admin already exists, skipping seed.");
            }
        } catch (Exception e) {
            log.error("Failed to seed default admin: {}", e.getMessage(), e);
        }
    }
}
