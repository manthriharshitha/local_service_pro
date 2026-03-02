package com.localserviceprovider.backend.dto;

import java.util.List;

public class AdminMonitoringResponse {
    private List<AdminUserResponse> recentUsers;
    private List<AdminServiceResponse> recentServices;
    private List<AdminBookingResponse> recentBookings;

    public AdminMonitoringResponse(List<AdminUserResponse> recentUsers,
                                   List<AdminServiceResponse> recentServices,
                                   List<AdminBookingResponse> recentBookings) {
        this.recentUsers = recentUsers;
        this.recentServices = recentServices;
        this.recentBookings = recentBookings;
    }

    public List<AdminUserResponse> getRecentUsers() {
        return recentUsers;
    }

    public List<AdminServiceResponse> getRecentServices() {
        return recentServices;
    }

    public List<AdminBookingResponse> getRecentBookings() {
        return recentBookings;
    }
}
