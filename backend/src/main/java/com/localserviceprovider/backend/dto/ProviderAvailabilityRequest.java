package com.localserviceprovider.backend.dto;

public class ProviderAvailabilityRequest {
    private String workingDays;
    private String workingHours;
    private Boolean emergencyServiceEnabled;

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public Boolean getEmergencyServiceEnabled() {
        return emergencyServiceEnabled;
    }

    public void setEmergencyServiceEnabled(Boolean emergencyServiceEnabled) {
        this.emergencyServiceEnabled = emergencyServiceEnabled;
    }
}
