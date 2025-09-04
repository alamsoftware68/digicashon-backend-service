package com.repairapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * OTP verification request DTO
 * Step 2 of the signup process
 */
public class OTPVerificationRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile number should be valid")
    private String mobile;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otpCode;

    // Constructors
    public OTPVerificationRequest() {}

    public OTPVerificationRequest(String mobile, String otpCode) {
        this.mobile = mobile;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
