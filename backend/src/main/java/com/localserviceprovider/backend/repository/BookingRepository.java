package com.localserviceprovider.backend.repository;

import com.localserviceprovider.backend.model.Booking;
import com.localserviceprovider.backend.model.BookingStatus;
import com.localserviceprovider.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByProvider(User provider);
    long countByCustomer(User customer);
    long countByCustomerAndStatus(User customer, BookingStatus status);
    long countByStatus(BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByProviderAndBookingDateBetween(User provider, LocalDate fromDate, LocalDate toDate);
    List<Booking> findByBookingDateBetween(LocalDate fromDate, LocalDate toDate);
    List<Booking> findByProviderAndStatus(User provider, BookingStatus status);
    List<Booking> findByProviderAndStatusAndBookingDateBetween(User provider,
                                                               BookingStatus status,
                                                               LocalDate fromDate,
                                                               LocalDate toDate);
}
