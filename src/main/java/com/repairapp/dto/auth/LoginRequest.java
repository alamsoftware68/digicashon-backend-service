package com.repairapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Login request DTO
 */
public class LoginRequest {

    private String mobile;

    private String password;

    private boolean rememberMe = false;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String mobile, String password, boolean rememberMe) {
        this.mobile = mobile;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    // Getters and Setters
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    // Keep getUsername for backward compatibility (maps to mobile)
    public String getUsername() {
        return mobile;
    }

    public void setUsername(String username) {
        this.mobile = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
