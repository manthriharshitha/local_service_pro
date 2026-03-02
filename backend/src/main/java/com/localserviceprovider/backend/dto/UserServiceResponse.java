package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.ServiceCategory;

import java.math.BigDecimal;

public class UserServiceResponse {
    private Long id;
    private String title;
    private String description;
    private ServiceCategory category;
    private String providerName;
    private BigDecimal price;
    private Double averageRating;

    public UserServiceResponse(Long id,
                               String title,
                               String description,
                               ServiceCategory category,
                               String providerName,
                               BigDecimal price,
                               Double averageRating) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.providerName = providerName;
        this.price = price;
        this.averageRating = averageRating;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public String getProviderName() {
        return providerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Double getAverageRating() {
        return averageRating;
    }
}
