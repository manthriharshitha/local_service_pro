package com.localserviceprovider.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public class AdminAnalyticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Map<String, BigDecimal> revenuePerProvider;
    private String mostBookedService;
    private String leastBookedService;

    public AdminAnalyticsResponse(BigDecimal totalRevenue,
                                  BigDecimal monthlyRevenue,
                                  Map<String, BigDecimal> revenuePerProvider,
                                  String mostBookedService,
                                  String leastBookedService) {
        this.totalRevenue = totalRevenue;
        this.monthlyRevenue = monthlyRevenue;
        this.revenuePerProvider = revenuePerProvider;
        this.mostBookedService = mostBookedService;
        this.leastBookedService = leastBookedService;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public Map<String, BigDecimal> getRevenuePerProvider() {
        return revenuePerProvider;
    }

    public String getMostBookedService() {
        return mostBookedService;
    }

    public String getLeastBookedService() {
        return leastBookedService;
    }
}
