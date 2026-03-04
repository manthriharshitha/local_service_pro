package com.localserviceprovider.backend.controller;

import com.localserviceprovider.backend.dto.BookingStatusUpdateRequest;
import com.localserviceprovider.backend.dto.ChangePasswordRequest;
import com.localserviceprovider.backend.dto.ProviderAvailabilityRequest;
import com.localserviceprovider.backend.dto.ProviderAvailabilityResponse;
import com.localserviceprovider.backend.dto.ProviderOverviewResponse;
import com.localserviceprovider.backend.dto.ProviderReviewResponse;
import com.localserviceprovider.backend.dto.ProviderReviewsSummaryResponse;
import com.localserviceprovider.backend.dto.ServiceRequest;
import com.localserviceprovider.backend.dto.ServiceStatusUpdateRequest;
import com.localserviceprovider.backend.dto.UpdateUserProfileRequest;
import com.localserviceprovider.backend.dto.UserProfileResponse;
import com.localserviceprovider.backend.model.Booking;
import com.localserviceprovider.backend.model.BookingStatus;
import com.localserviceprovider.backend.model.PaymentStatus;
import com.localserviceprovider.backend.model.Review;
import com.localserviceprovider.backend.model.Service;
import com.localserviceprovider.backend.model.ServiceStatus;
import com.localserviceprovider.backend.model.User;
import com.localserviceprovider.backend.repository.BookingRepository;
import com.localserviceprovider.backend.repository.ReviewRepository;
import com.localserviceprovider.backend.repository.ServiceRepository;
import com.localserviceprovider.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/provider")
public class ProviderController {

    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProviderController(ServiceRepository serviceRepository,
                              BookingRepository bookingRepository,
                              ReviewRepository reviewRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('PROVIDER')")
    public ProviderOverviewResponse getOverview(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);

        List<Service> services = serviceRepository.findByProvider(provider);
        List<Booking> bookings = bookingRepository.findByProvider(provider);
        List<Booking> completedBookings = bookingRepository.findByProviderAndStatus(provider, BookingStatus.COMPLETED);

        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
        List<Booking> thisMonthCompleted = bookingRepository.findByProviderAndStatusAndBookingDateBetween(
                provider,
                BookingStatus.COMPLETED,
                firstDay,
                lastDay
        );

        BigDecimal totalEarnings = completedBookings.stream()
                .map(booking -> booking.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal thisMonthEarnings = thisMonthCompleted.stream()
                .map(booking -> booking.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProviderOverviewResponse(
                provider.getName(),
                services.size(),
                bookings.size(),
                completedBookings.size(),
                totalEarnings,
                thisMonthEarnings
        );
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('PROVIDER')")
    public Service addService(@RequestBody ServiceRequest request,
                              @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);

        Service service = new Service();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setCategory(request.getCategory());
        service.setStatus(request.getStatus() == null ? ServiceStatus.ACTIVE : request.getStatus());
        service.setCreatedAt(LocalDateTime.now());
        service.setProvider(provider);

        return serviceRepository.save(service);
    }

    @GetMapping("/services")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<Service> getMyServices(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        return serviceRepository.findByProvider(provider);
    }

    @PutMapping("/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public Service updateService(@PathVariable Long id,
                                 @RequestBody ServiceRequest request,
                                 @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        validateServiceOwner(service, provider);

        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setPrice(request.getPrice());
        service.setStatus(request.getStatus() == null ? service.getStatus() : request.getStatus());
        return serviceRepository.save(service);
    }

    @DeleteMapping("/services/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public Map<String, String> deleteService(@PathVariable Long id,
                                              @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        validateServiceOwner(service, provider);

        serviceRepository.delete(service);
        return Map.of("message", "Service deleted");
    }

    @PutMapping("/services/{id}/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public Service updateServiceStatus(@PathVariable Long id,
                                       @RequestBody ServiceStatusUpdateRequest request,
                                       @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        validateServiceOwner(service, provider);

        if (request.getStatus() == null) {
            throw new RuntimeException("Service status is required");
        }
        service.setStatus(request.getStatus());
        return serviceRepository.save(service);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<Booking> getAssignedBookings(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        return bookingRepository.findByProvider(provider);
    }

    @PutMapping("/bookings/{id}/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public Booking updateBookingStatus(@PathVariable Long id,
                                       @RequestBody BookingStatusUpdateRequest request,
                                       @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized booking access");
        }
        if (request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking status is required");
        }

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus nextStatus = request.getStatus();

        if (currentStatus == BookingStatus.COMPLETED
                || currentStatus == BookingStatus.CANCELLED
                || currentStatus == BookingStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finalized bookings cannot be modified");
        }

        if (nextStatus == BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move booking back to PENDING");
        }

        boolean validTransition =
                (currentStatus == BookingStatus.PENDING
                        && (nextStatus == BookingStatus.ACCEPTED
                        || nextStatus == BookingStatus.REJECTED
                        || nextStatus == BookingStatus.CANCELLED))
                        || (currentStatus == BookingStatus.ACCEPTED
                        && (nextStatus == BookingStatus.COMPLETED
                        || nextStatus == BookingStatus.CANCELLED));

        if (!validTransition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status transition");
        }

        booking.setStatus(nextStatus);
        if (nextStatus == BookingStatus.COMPLETED && booking.getPaymentStatus() == null) {
            booking.setPaymentStatus(PaymentStatus.PAID);
        }
        if (nextStatus == BookingStatus.CANCELLED || nextStatus == BookingStatus.REJECTED) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        return bookingRepository.save(booking);
    }

    @GetMapping("/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ProviderAvailabilityResponse getAvailability(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        return new ProviderAvailabilityResponse(
                provider.getWorkingDays(),
                provider.getWorkingHours(),
                Boolean.TRUE.equals(provider.getEmergencyServiceEnabled())
        );
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public UserProfileResponse getProfile(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        return new UserProfileResponse(provider.getName(), provider.getEmail(), provider.getPhone());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserProfileRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        if (request.getName() != null && !request.getName().isBlank()) {
            provider.setName(request.getName());
        }
        userRepository.save(provider);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), provider.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }

        provider.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(provider);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> updateAvailability(@RequestBody ProviderAvailabilityRequest request,
                                                @AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        provider.setWorkingDays(request.getWorkingDays());
        provider.setWorkingHours(request.getWorkingHours());
        provider.setEmergencyServiceEnabled(Boolean.TRUE.equals(request.getEmergencyServiceEnabled()));
        userRepository.save(provider);
        return ResponseEntity.ok(Map.of("message", "Availability updated"));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('PROVIDER')")
    public ProviderReviewsSummaryResponse getProviderReviews(@AuthenticationPrincipal User currentUser) {
        User provider = getProvider(currentUser);
        List<Review> reviews = reviewRepository.findByProviderOrderByCreatedAtDesc(provider);

        List<ProviderReviewResponse> reviewResponses = reviews.stream()
                .map(review -> new ProviderReviewResponse(
                        review.getId(),
                        review.getService() == null ? null : review.getService().getId(),
                        review.getService() == null ? null : review.getService().getTitle(),
                        review.getCustomer() == null ? "Unknown" : review.getCustomer().getName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()
                ))
                .toList();

        Double average = reviewRepository.findAverageRatingByProvider(provider);
        if (average == null) {
            average = 0.0;
        }

        return new ProviderReviewsSummaryResponse(average, (long) reviewResponses.size(), reviewResponses);
    }

    private User getProvider(User currentUser) {
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    private void validateServiceOwner(Service service, User provider) {
        if (!service.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized service access");
        }
    }
}
