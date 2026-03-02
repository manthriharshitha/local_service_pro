package com.localserviceprovider.backend.dto;

import java.math.BigDecimal;

public class ProviderOverviewResponse {
    private String providerName;
    private long totalServices;
    private long totalBookings;
    private long totalCompletedBookings;
    private BigDecimal totalEarnings;
    private BigDecimal thisMonthEarnings;

    public ProviderOverviewResponse(String providerName,
                                    long totalServices,
                                    long totalBookings,
                                    long totalCompletedBookings,
                                    BigDecimal totalEarnings,
                                    BigDecimal thisMonthEarnings) {
        this.providerName = providerName;
        this.totalServices = totalServices;
        this.totalBookings = totalBookings;
        this.totalCompletedBookings = totalCompletedBookings;
        this.totalEarnings = totalEarnings;
        this.thisMonthEarnings = thisMonthEarnings;
    }

    public String getProviderName() {
        return providerName;
    }

    public long getTotalServices() {
        return totalServices;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public long getTotalCompletedBookings() {
        return totalCompletedBookings;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public BigDecimal getThisMonthEarnings() {
        return thisMonthEarnings;
    }
}
