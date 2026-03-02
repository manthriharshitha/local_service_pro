package com.localserviceprovider.backend;

import com.localserviceprovider.backend.model.Role;
import com.localserviceprovider.backend.model.User;
import com.localserviceprovider.backend.model.UserStatus;
import com.localserviceprovider.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class LocalServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalServiceProviderApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JdbcTemplate jdbcTemplate) {
        return args -> {
            repairReviewProviderForeignKey(jdbcTemplate);

            List<User> users = userRepository.findAll();
            for (User existingUser : users) {
                if (existingUser.getRole() == null) {
                    existingUser.setRole(Role.USER);
                }
                if (existingUser.getStatus() == null) {
                    existingUser.setStatus(UserStatus.ACTIVE);
                }
                if (existingUser.getRegistrationDate() == null) {
                    existingUser.setRegistrationDate(LocalDateTime.now());
                }
                userRepository.save(existingUser);
            }

            if (userRepository.findByEmailIgnoreCase("admin@local.com").isEmpty()) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@local.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setRegistrationDate(LocalDateTime.now());
                userRepository.save(admin);
            } else {
                User admin = userRepository.findByEmailIgnoreCase("admin@local.com").orElseThrow();
                if (admin.getRole() != Role.ADMIN) {
                    admin.setRole(Role.ADMIN);
                }
                if (admin.getStatus() == null || admin.getStatus() == UserStatus.BLOCKED) {
                    admin.setStatus(UserStatus.ACTIVE);
                }
                if (admin.getRegistrationDate() == null) {
                    admin.setRegistrationDate(LocalDateTime.now());
                }
                userRepository.save(admin);
            }
        };
    }

    private void repairReviewProviderForeignKey(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE reviews DROP FOREIGN KEY FKrsomnwai2734u8lqdqu5yd96x");
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> fkRows = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME, REFERENCED_TABLE_NAME " +
                        "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = 'reviews' " +
                        "AND COLUMN_NAME = 'provider_id' " +
                        "AND REFERENCED_TABLE_NAME IS NOT NULL"
        );

        boolean referencesUsers = fkRows.stream()
                .anyMatch(row -> "users".equalsIgnoreCase(String.valueOf(row.get("REFERENCED_TABLE_NAME"))));

        for (Map<String, Object> row : fkRows) {
            String constraintName = String.valueOf(row.get("CONSTRAINT_NAME"));
            if (constraintName != null && !constraintName.isBlank()) {
                try {
                    jdbcTemplate.execute("ALTER TABLE reviews DROP FOREIGN KEY `" + constraintName + "`");
                } catch (Exception ignored) {
                }
            }
        }

        if (!referencesUsers) {
            try {
                jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT FKrsomnwai2734u8lqdqu5yd96x " +
                        "FOREIGN KEY (provider_id) REFERENCES users(id)");
            } catch (Exception ignored) {
            }
        }
    }
}
