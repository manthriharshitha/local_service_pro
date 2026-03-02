package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.ServiceCategory;
import com.localserviceprovider.backend.model.ServiceStatus;

import java.math.BigDecimal;

public class AdminServiceResponse {
    private Long id;
    private String title;
    private ServiceCategory category;
    private BigDecimal price;
    private String providerName;
    private ServiceStatus status;
    private long totalBookings;

    public AdminServiceResponse(Long id,
                                String title,
                                ServiceCategory category,
                                BigDecimal price,
                                String providerName,
                                ServiceStatus status,
                                long totalBookings) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.providerName = providerName;
        this.status = status;
        this.totalBookings = totalBookings;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getProviderName() {
        return providerName;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public long getTotalBookings() {
        return totalBookings;
    }
}
