package com.localserviceprovider.backend.dto;

public class ProviderAvailabilityResponse {
    private String workingDays;
    private String workingHours;
    private boolean emergencyServiceEnabled;

    public ProviderAvailabilityResponse(String workingDays, String workingHours, boolean emergencyServiceEnabled) {
        this.workingDays = workingDays;
        this.workingHours = workingHours;
        this.emergencyServiceEnabled = emergencyServiceEnabled;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public boolean isEmergencyServiceEnabled() {
        return emergencyServiceEnabled;
    }
}
