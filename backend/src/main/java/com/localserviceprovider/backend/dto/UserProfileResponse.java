package com.localserviceprovider.backend.dto;

public class UserProfileResponse {
    private String name;
    private String email;
    private String phone;

    public UserProfileResponse(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
