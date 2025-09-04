package com.repairapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Mobile number signup initiation request DTO
 * Step 1 of the signup process
 */
public class MobileSignupRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile number should be valid (10-15 digits)")
    private String mobile;

    // Constructors
    public MobileSignupRequest() {}

    public MobileSignupRequest(String mobile) {
        this.mobile = mobile;
    }

    // Getters and Setters
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
