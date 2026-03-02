package com.localserviceprovider.backend.controller;

import com.localserviceprovider.backend.dto.*;
import com.localserviceprovider.backend.model.*;
import com.localserviceprovider.backend.repository.BookingRepository;
import com.localserviceprovider.backend.repository.ReviewRepository;
import com.localserviceprovider.backend.repository.ServiceRepository;
import com.localserviceprovider.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(ServiceRepository serviceRepository,
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
    public UserOverviewResponse getOverview(@AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        return new UserOverviewResponse(
                user.getName(),
                bookingRepository.countByCustomer(user),
                bookingRepository.countByCustomerAndStatus(user, BookingStatus.PENDING),
                bookingRepository.countByCustomerAndStatus(user, BookingStatus.COMPLETED),
                bookingRepository.countByCustomerAndStatus(user, BookingStatus.CANCELLED)
        );
    }

    @GetMapping("/services")
    public List<UserServiceResponse> getActiveServices(@RequestParam(required = false) String search,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String sort) {
        List<Service> services = serviceRepository.findByStatus(ServiceStatus.ACTIVE);

        List<Service> filtered = services.stream()
                .filter(service -> search == null || search.isBlank()
                        || service.getTitle().toLowerCase().contains(search.toLowerCase()))
                .filter(service -> category == null || category.isBlank()
                        || service.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());

        if ("low".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(Service::getPrice));
        } else if ("high".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(Service::getPrice).reversed());
        }

        return filtered.stream().map(this::toUserServiceResponse).collect(Collectors.toList());
    }

    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        User customer = getCurrentUser(currentUser);
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (service.getStatus() != ServiceStatus.ACTIVE) {
            throw new RuntimeException("Service is not active");
        }

        Booking booking = new Booking();
        booking.setService(service);
        booking.setCustomer(customer);
        booking.setProvider(service.getProvider());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingDate(request.getBookingDate() == null ? LocalDate.now() : request.getBookingDate());
        booking.setBookingTime(request.getBookingTime() == null ? LocalTime.now() : request.getBookingTime());
        booking.setAddress(request.getAddress() == null || request.getAddress().isBlank()
                ? "Address not provided"
                : request.getAddress());
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Booking created successfully", "bookingId", booking.getId()));
    }

    @GetMapping("/bookings")
    public List<UserBookingResponse> getMyBookings(@AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        return bookingRepository.findByCustomer(user)
                .stream()
                .map(booking -> new UserBookingResponse(
                        booking.getId(),
                        booking.getService().getTitle(),
                        booking.getProvider().getName(),
                        booking.getBookingDate(),
                        booking.getBookingTime(),
                        booking.getAddress(),
                        booking.getStatus(),
                        booking.getCancellationReason()))
                .collect(Collectors.toList());
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           @RequestBody CancelBookingRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized booking access");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        booking.setCancellationReason(request.getCancellationReason() == null || request.getCancellationReason().isBlank()
                ? "Cancelled by user"
                : request.getCancellationReason());
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Booking cancelled"));
    }

    @PostMapping("/review")
    public ResponseEntity<?> leaveReview(@RequestBody ReviewRequest request,
                                         @AuthenticationPrincipal User currentUser) {
        return submitReview(request, currentUser);
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> leaveReviewV2(@RequestBody ReviewRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        return submitReview(request, currentUser);
    }

    private ResponseEntity<?> submitReview(ReviewRequest request, User currentUser) {
        try {
            User customer = getCurrentUser(currentUser);
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getCustomer().getId().equals(customer.getId())) {
                throw new RuntimeException("Unauthorized booking access");
            }

            if (booking.getStatus() != BookingStatus.COMPLETED) {
                throw new RuntimeException("Review allowed only for completed bookings");
            }

            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5");
            }

            Review review = reviewRepository.findByServiceAndCustomer(booking.getService(), customer).orElseGet(Review::new);
            review.setService(booking.getService());
            review.setProvider(booking.getProvider());
            review.setCustomer(customer);
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setCreatedAt(LocalDateTime.now());
            reviewRepository.save(review);

            return ResponseEntity.ok(Map.of("message", "Review submitted"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage() == null ? "Unable to submit review" : ex.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public List<String> getNotifications(@AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        return bookingRepository.findByCustomer(user)
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.ACCEPTED
                        || booking.getStatus() == BookingStatus.COMPLETED
                        || booking.getStatus() == BookingStatus.REJECTED)
                .map(booking -> switch (booking.getStatus()) {
                    case ACCEPTED -> "Booking #" + booking.getId() + " has been accepted";
                    case COMPLETED -> "Booking #" + booking.getId() + " has been completed";
                    case REJECTED -> "Booking #" + booking.getId() + " has been rejected";
                    default -> "";
                })
                .filter(text -> !text.isBlank())
                .collect(Collectors.toList());
    }

    @GetMapping("/profile")
    public UserProfileResponse getProfile(@AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        return new UserProfileResponse(user.getName(), user.getEmail(), user.getPhone());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserProfileRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        user.setName(request.getName() == null || request.getName().isBlank() ? user.getName() : request.getName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal User currentUser) {
        User user = getCurrentUser(currentUser);
        if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    private UserServiceResponse toUserServiceResponse(Service service) {
        List<Review> reviews = reviewRepository.findByService(service);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        return new UserServiceResponse(
                service.getId(),
                service.getTitle(),
                service.getDescription(),
                service.getCategory(),
                service.getProvider().getName(),
                service.getPrice(),
                average == 0 ? null : average
        );
    }

    private User getCurrentUser(User currentUser) {
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
