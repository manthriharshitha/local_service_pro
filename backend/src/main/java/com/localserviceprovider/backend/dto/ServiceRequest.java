package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.ServiceCategory;
import com.localserviceprovider.backend.model.ServiceStatus;

import java.math.BigDecimal;

public class ServiceRequest {
    private String title;
    private String description;
    private ServiceCategory category;
    private BigDecimal price;
    private ServiceStatus status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
}
