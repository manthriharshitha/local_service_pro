package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.Role;

public class AdminUpdateUserRoleRequest {
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
