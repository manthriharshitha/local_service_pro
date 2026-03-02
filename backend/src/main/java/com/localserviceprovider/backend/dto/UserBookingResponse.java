package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.BookingStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class UserBookingResponse {
    private Long id;
    private String serviceTitle;
    private String providerName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String address;
    private BookingStatus status;
    private String cancellationReason;

    public UserBookingResponse(Long id,
                               String serviceTitle,
                               String providerName,
                               LocalDate bookingDate,
                               LocalTime bookingTime,
                               String address,
                               BookingStatus status,
                               String cancellationReason) {
        this.id = id;
        this.serviceTitle = serviceTitle;
        this.providerName = providerName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.address = address;
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public Long getId() {
        return id;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getProviderName() {
        return providerName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public String getAddress() {
        return address;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }
}
