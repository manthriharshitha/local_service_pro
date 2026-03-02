package com.localserviceprovider.backend.dto;

import java.time.LocalDateTime;

public class ProviderReviewResponse {
    private Long reviewId;
    private Long serviceId;
    private String serviceTitle;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ProviderReviewResponse(Long reviewId,
                                  Long serviceId,
                                  String serviceTitle,
                                  String customerName,
                                  Integer rating,
                                  String comment,
                                  LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}