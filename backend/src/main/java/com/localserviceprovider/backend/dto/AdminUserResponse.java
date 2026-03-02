package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.Role;
import com.localserviceprovider.backend.model.UserStatus;

import java.time.LocalDateTime;

public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime registrationDate;
    private UserStatus status;

    public AdminUserResponse(Long id,
                             String name,
                             String email,
                             Role role,
                             LocalDateTime registrationDate,
                             UserStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public UserStatus getStatus() {
        return status;
    }
}
