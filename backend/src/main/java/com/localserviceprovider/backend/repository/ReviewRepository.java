package com.localserviceprovider.backend.repository;

import com.localserviceprovider.backend.model.Review;
import com.localserviceprovider.backend.model.Service;
import com.localserviceprovider.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByService(Service service);
    Optional<Review> findByServiceAndCustomer(Service service, User customer);
    List<Review> findByProviderOrderByCreatedAtDesc(User provider);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider = :provider")
    Double findAverageRatingByProvider(@Param("provider") User provider);
}
