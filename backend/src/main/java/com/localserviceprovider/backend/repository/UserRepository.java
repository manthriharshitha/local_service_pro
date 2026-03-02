package com.localserviceprovider.backend.repository;

import com.localserviceprovider.backend.model.User;
import com.localserviceprovider.backend.model.Role;
import com.localserviceprovider.backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(Role role);
    long countByStatus(UserStatus status);
}
