package com.localserviceprovider.backend.controller;

import com.localserviceprovider.backend.model.Service;
import com.localserviceprovider.backend.repository.ServiceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','PROVIDER','ADMIN')")
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }
}
