package com.localserviceprovider.backend.repository;

import com.localserviceprovider.backend.model.Service;
import com.localserviceprovider.backend.model.ServiceCategory;
import com.localserviceprovider.backend.model.ServiceStatus;
import com.localserviceprovider.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByProvider(User provider);
    List<Service> findByStatus(ServiceStatus status);
    List<Service> findByCategory(ServiceCategory category);
    long countByStatus(ServiceStatus status);
}
