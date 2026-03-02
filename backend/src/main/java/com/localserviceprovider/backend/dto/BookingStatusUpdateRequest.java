package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.BookingStatus;

public class BookingStatusUpdateRequest {
    private BookingStatus status;

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
