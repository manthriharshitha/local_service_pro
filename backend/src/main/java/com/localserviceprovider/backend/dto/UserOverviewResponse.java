package com.localserviceprovider.backend.dto;

public class UserOverviewResponse {
    private String userName;
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long cancelledBookings;

    public UserOverviewResponse(String userName,
                                long totalBookings,
                                long pendingBookings,
                                long completedBookings,
                                long cancelledBookings) {
        this.userName = userName;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.completedBookings = completedBookings;
        this.cancelledBookings = cancelledBookings;
    }

    public String getUserName() {
        return userName;
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

    public long getCancelledBookings() {
        return cancelledBookings;
    }
}
