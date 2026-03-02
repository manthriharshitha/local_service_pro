package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.ServiceStatus;

public class AdminUpdateServiceStatusRequest {
    private ServiceStatus status;

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
}
