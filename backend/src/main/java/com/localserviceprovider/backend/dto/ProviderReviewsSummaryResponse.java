package com.localserviceprovider.backend.dto;

import java.util.List;

public class ProviderReviewsSummaryResponse {
    private Double averageRating;
    private Long totalReviews;
    private List<ProviderReviewResponse> reviews;

    public ProviderReviewsSummaryResponse(Double averageRating,
                                          Long totalReviews,
                                          List<ProviderReviewResponse> reviews) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.reviews = reviews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public List<ProviderReviewResponse> getReviews() {
        return reviews;
    }
}