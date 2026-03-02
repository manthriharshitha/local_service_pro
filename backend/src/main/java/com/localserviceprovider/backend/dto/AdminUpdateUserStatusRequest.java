package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.UserStatus;

public class AdminUpdateUserStatusRequest {
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
