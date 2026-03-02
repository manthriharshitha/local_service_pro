package com.localserviceprovider.backend.controller;

import com.localserviceprovider.backend.dto.BookingRequest;
import com.localserviceprovider.backend.model.Booking;
import com.localserviceprovider.backend.model.BookingStatus;
import com.localserviceprovider.backend.model.PaymentStatus;
import com.localserviceprovider.backend.model.Service;
import com.localserviceprovider.backend.model.User;
import com.localserviceprovider.backend.repository.BookingRepository;
import com.localserviceprovider.backend.repository.ServiceRepository;
import com.localserviceprovider.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public BookingController(BookingRepository bookingRepository,
                             ServiceRepository serviceRepository,
                             UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        User customer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Booking booking = new Booking();
        booking.setService(service);
        booking.setCustomer(customer);
        booking.setProvider(service.getProvider());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingDate(request.getBookingDate() != null ? request.getBookingDate() : LocalDate.now());
        booking.setBookingTime(request.getBookingTime() != null ? request.getBookingTime() : LocalTime.now());
        booking.setAddress(request.getAddress() == null || request.getAddress().isBlank() ? "Address not provided" : request.getAddress());
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Booking created", "bookingId", booking.getId()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<Booking> myBookings(@AuthenticationPrincipal User currentUser) {
        User customer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return bookingRepository.findByCustomer(customer);
    }
}
