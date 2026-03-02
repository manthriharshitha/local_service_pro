package com.localserviceprovider.backend.dto;

import java.math.BigDecimal;

public class AdminOverviewResponse {
    private long totalUsers;
    private long totalProviders;
    private long totalServices;
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private BigDecimal totalRevenue;

    public AdminOverviewResponse(long totalUsers,
                                 long totalProviders,
                                 long totalServices,
                                 long totalBookings,
                                 long pendingBookings,
                                 long completedBookings,
                                 BigDecimal totalRevenue) {
        this.totalUsers = totalUsers;
        this.totalProviders = totalProviders;
        this.totalServices = totalServices;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.completedBookings = completedBookings;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProviders() {
        return totalProviders;
    }

    public long getTotalServices() {
        return totalServices;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
