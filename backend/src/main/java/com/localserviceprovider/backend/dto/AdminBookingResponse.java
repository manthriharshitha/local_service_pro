package com.localserviceprovider.backend.dto;

import com.localserviceprovider.backend.model.BookingStatus;
import com.localserviceprovider.backend.model.PaymentStatus;

import java.time.LocalDate;

public class AdminBookingResponse {
    private Long id;
    private String serviceTitle;
    private String customerName;
    private String providerName;
    private LocalDate bookingDate;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String address;
    private String bookingTime;
    private String cancellationReason;

    public AdminBookingResponse(Long id,
                                String serviceTitle,
                                String customerName,
                                String providerName,
                                LocalDate bookingDate,
                                BookingStatus status,
                                PaymentStatus paymentStatus,
                                String address,
                                String bookingTime,
                                String cancellationReason) {
        this.id = id;
        this.serviceTitle = serviceTitle;
        this.customerName = customerName;
        this.providerName = providerName;
        this.bookingDate = bookingDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.address = address;
        this.bookingTime = bookingTime;
        this.cancellationReason = cancellationReason;
    }

    public Long getId() {
        return id;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getAddress() {
        return address;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }
}
