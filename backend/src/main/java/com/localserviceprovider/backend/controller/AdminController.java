package com.localserviceprovider.backend.controller;

import com.localserviceprovider.backend.dto.*;
import com.localserviceprovider.backend.model.*;
import com.localserviceprovider.backend.repository.BookingRepository;
import com.localserviceprovider.backend.repository.ServiceRepository;
import com.localserviceprovider.backend.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           ServiceRepository serviceRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse overview() {
        List<Booking> completed = bookingRepository.findByStatus(BookingStatus.COMPLETED);
        BigDecimal totalRevenue = completed.stream()
                .map(booking -> booking.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminOverviewResponse(
                userRepository.countByRole(Role.USER),
                userRepository.countByRole(Role.PROVIDER),
                serviceRepository.count(),
                bookingRepository.count(),
                bookingRepository.countByStatus(BookingStatus.PENDING),
                bookingRepository.countByStatus(BookingStatus.COMPLETED),
                totalRevenue
        );
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users(@RequestParam(required = false) String search,
                                         @RequestParam(required = false) Role role,
                                         @RequestParam(defaultValue = "desc") String sort) {
        List<User> users = new ArrayList<>(userRepository.findAll());

        if (search != null && !search.isBlank()) {
            users = users.stream()
                    .filter(user -> (user.getName() != null && user.getName().toLowerCase().contains(search.toLowerCase()))
                            || user.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (role != null) {
            users = users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
        }

        users.sort(Comparator.comparing(User::getRegistrationDate, Comparator.nullsLast(Comparator.naturalOrder())));
        if (!"asc".equalsIgnoreCase(sort)) {
            users.sort(Comparator.comparing(User::getRegistrationDate,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return users.stream().map(this::toAdminUserResponse).collect(Collectors.toList());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentAdmin) {
        if (currentAdmin.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin cannot delete themselves"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id,
                                              @RequestBody AdminUpdateUserStatusRequest request,
                                              @AuthenticationPrincipal User currentAdmin) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (currentAdmin.getId().equals(user.getId()) && request.getStatus() == UserStatus.BLOCKED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin cannot block themselves"));
        }
        user.setStatus(request.getStatus());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                            @RequestBody AdminUpdateUserRoleRequest request,
                                            @AuthenticationPrincipal User currentAdmin) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (currentAdmin.getId().equals(user.getId()) && request.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin cannot demote themselves"));
        }
        user.setRole(request.getRole());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User role updated"));
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestBody AdminResetPasswordRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        String password = request.getNewPassword() == null || request.getNewPassword().isBlank()
                ? "Temp@123"
                : request.getNewPassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @GetMapping("/services")
    public List<AdminServiceResponse> services(@RequestParam(required = false) ServiceCategory category,
                                               @RequestParam(required = false) String provider,
                                               @RequestParam(defaultValue = "none") String sort) {
        List<Service> services = new ArrayList<>(serviceRepository.findAll());

        if (category != null) {
            services = services.stream().filter(service -> service.getCategory() == category).collect(Collectors.toList());
        }
        if (provider != null && !provider.isBlank()) {
            services = services.stream()
                    .filter(service -> service.getProvider().getName() != null
                            && service.getProvider().getName().toLowerCase().contains(provider.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if ("low".equalsIgnoreCase(sort)) {
            services.sort(Comparator.comparing(Service::getPrice));
        } else if ("high".equalsIgnoreCase(sort)) {
            services.sort(Comparator.comparing(Service::getPrice).reversed());
        }

        return services.stream().map(this::toAdminServiceResponse).collect(Collectors.toList());
    }

    @DeleteMapping("/services/{id}")
    public Map<String, String> deleteService(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return Map.of("message", "Service deleted");
    }

    @PutMapping("/services/{id}/status")
    public ResponseEntity<?> updateServiceStatus(@PathVariable Long id,
                                                 @RequestBody AdminUpdateServiceStatusRequest request) {
        Service service = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
        service.setStatus(request.getStatus());
        serviceRepository.save(service);
        return ResponseEntity.ok(Map.of("message", "Service status updated"));
    }

    @GetMapping("/bookings")
    public List<AdminBookingResponse> bookings(@RequestParam(required = false) BookingStatus status,
                                               @RequestParam(required = false) String provider,
                                               @RequestParam(required = false) LocalDate fromDate,
                                               @RequestParam(required = false) LocalDate toDate) {
        List<Booking> bookings = new ArrayList<>(bookingRepository.findAll());

        if (status != null) {
            bookings = bookings.stream().filter(booking -> booking.getStatus() == status).collect(Collectors.toList());
        }

        if (provider != null && !provider.isBlank()) {
            bookings = bookings.stream().filter(booking -> booking.getProvider().getName() != null
                            && booking.getProvider().getName().toLowerCase().contains(provider.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (fromDate != null && toDate != null) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getBookingDate() != null
                            && !booking.getBookingDate().isBefore(fromDate)
                            && !booking.getBookingDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        bookings.sort(Comparator.comparing(Booking::getBookingDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return bookings.stream().map(this::toAdminBookingResponse).collect(Collectors.toList());
    }

    @GetMapping("/bookings/{id}")
    public AdminBookingResponse bookingDetails(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        return toAdminBookingResponse(booking);
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id,
                                                 @RequestBody AdminUpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(request.getStatus());

        if (request.getStatus() == BookingStatus.CANCELLED) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        if (request.getStatus() == BookingStatus.COMPLETED && booking.getPaymentStatus() == null) {
            booking.setPaymentStatus(PaymentStatus.PAID);
        }

        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("message", "Booking status updated"));
    }

    @GetMapping("/analytics")
    public AdminAnalyticsResponse analytics() {
        List<Booking> allBookings = bookingRepository.findAll();
        List<Booking> completed = allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = completed.stream()
                .map(booking -> booking.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth thisMonth = YearMonth.now();
        BigDecimal monthlyRevenue = completed.stream()
                .filter(booking -> booking.getBookingDate() != null
                        && YearMonth.from(booking.getBookingDate()).equals(thisMonth))
                .map(booking -> booking.getService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> revenuePerProvider = completed.stream()
                .collect(Collectors.groupingBy(booking -> booking.getProvider().getName(),
                        Collectors.mapping(booking -> booking.getService().getPrice(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        Map<String, Long> serviceBookingCount = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getService().getTitle(), Collectors.counting()));

        String mostBooked = serviceBookingCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String leastBooked = serviceBookingCount.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return new AdminAnalyticsResponse(totalRevenue, monthlyRevenue, revenuePerProvider, mostBooked, leastBooked);
    }

    @GetMapping("/monitoring")
    public AdminMonitoringResponse monitoring() {
        List<AdminUserResponse> recentUsers = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::toAdminUserResponse)
                .collect(Collectors.toList());

        List<AdminServiceResponse> recentServices = serviceRepository.findAll().stream()
                .sorted(Comparator.comparing(Service::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::toAdminServiceResponse)
                .collect(Collectors.toList());

        List<AdminBookingResponse> recentBookings = bookingRepository.findAll().stream()
                .sorted(Comparator.comparing(Booking::getBookingDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::toAdminBookingResponse)
                .collect(Collectors.toList());

        return new AdminMonitoringResponse(recentUsers, recentServices, recentBookings);
    }

    @GetMapping(value = "/reports/users", produces = "text/csv")
    public ResponseEntity<byte[]> usersReport() {
        StringBuilder csv = new StringBuilder("User ID,Name,Email,Role,Registration Date,Status\n");
        for (User user : userRepository.findAll()) {
            csv.append(user.getId()).append(',')
                    .append(sanitizeCsv(user.getName())).append(',')
                    .append(sanitizeCsv(user.getEmail())).append(',')
                    .append(user.getRole()).append(',')
                    .append(user.getRegistrationDate()).append(',')
                    .append(user.getStatus()).append('\n');
        }
        return csvResponse("users-report.csv", csv.toString());
    }

    @GetMapping(value = "/reports/bookings", produces = "text/csv")
    public ResponseEntity<byte[]> bookingsReport() {
        StringBuilder csv = new StringBuilder("Booking ID,Service,Customer,Provider,Date,Status,Payment Status\n");
        for (Booking booking : bookingRepository.findAll()) {
            csv.append(booking.getId()).append(',')
                    .append(sanitizeCsv(booking.getService().getTitle())).append(',')
                    .append(sanitizeCsv(booking.getCustomer().getName())).append(',')
                    .append(sanitizeCsv(booking.getProvider().getName())).append(',')
                    .append(booking.getBookingDate()).append(',')
                    .append(booking.getStatus()).append(',')
                    .append(booking.getPaymentStatus()).append('\n');
        }
        return csvResponse("bookings-report.csv", csv.toString());
    }

    @GetMapping(value = "/reports/revenue", produces = "text/csv")
    public ResponseEntity<byte[]> revenueReport() {
        AdminAnalyticsResponse analytics = analytics();
        StringBuilder csv = new StringBuilder("Metric,Value\n");
        csv.append("Total Revenue,").append(analytics.getTotalRevenue()).append('\n');
        csv.append("Monthly Revenue,").append(analytics.getMonthlyRevenue()).append('\n');
        csv.append("Most Booked Service,").append(sanitizeCsv(analytics.getMostBookedService())).append('\n');
        csv.append("Least Booked Service,").append(sanitizeCsv(analytics.getLeastBookedService())).append('\n');
        csv.append("\nProvider,Revenue\n");
        analytics.getRevenuePerProvider().forEach((provider, revenue) ->
                csv.append(sanitizeCsv(provider)).append(',').append(revenue).append('\n'));
        return csvResponse("revenue-report.csv", csv.toString());
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRegistrationDate(),
                user.getStatus()
        );
    }

    private AdminServiceResponse toAdminServiceResponse(Service service) {
        long bookings = bookingRepository.findAll().stream()
                .filter(booking -> Objects.equals(booking.getService().getId(), service.getId()))
                .count();

        return new AdminServiceResponse(
                service.getId(),
                service.getTitle(),
                service.getCategory(),
                service.getPrice(),
                service.getProvider().getName(),
                service.getStatus(),
                bookings
        );
    }

    private AdminBookingResponse toAdminBookingResponse(Booking booking) {
        return new AdminBookingResponse(
                booking.getId(),
                booking.getService().getTitle(),
                booking.getCustomer().getName(),
                booking.getProvider().getName(),
                booking.getBookingDate(),
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getAddress(),
                booking.getBookingTime() == null ? null : booking.getBookingTime().toString(),
                booking.getCancellationReason()
        );
    }

    private ResponseEntity<byte[]> csvResponse(String fileName, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    private String sanitizeCsv(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
