package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.OTPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OTP Verification", description = "APIs for OTP generation and verification")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OTPController {
    
    private final OTPService otpService;
    
    @PostMapping("/generate")
    @Operation(summary = "Generate OTP", description = "Generate OTP for various verification purposes")
    public ResponseEntity<ApiResponse> generateOTP(
            @RequestBody @Valid GenerateOTPRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        OTPVerification otp = otpService.generateOTP(
            request.getIdentifier(), 
            request.getOtpType(), 
            userId, 
            sessionId);
        
        Map<String, Object> response = Map.of(
            "message", "OTP sent successfully to " + otpService.maskIdentifier(request.getIdentifier()),
            "expiresAt", otp.getExpiresAt(),
            "remainingAttempts", otp.getMaxAttempts()
        );
        
        return ResponseEntity.ok(ApiResponse.success("OTP generated successfully", response));
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP code for authentication")
    public ResponseEntity<ApiResponse> verifyOTP(
            @RequestBody @Valid VerifyOTPRequest request) {
        
        boolean isVerified = otpService.verifyOTP(
            request.getIdentifier(), 
            request.getOtpCode(), 
            request.getOtpType());
        
        if (isVerified) {
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
        } else {
            int remainingAttempts = otpService.getRemainingOTPAttempts(
                request.getIdentifier(), request.getOtpType());
            
            Map<String, Object> errorData = Map.of(
                "remainingAttempts", remainingAttempts,
                "message", remainingAttempts > 0 ? 
                    "Invalid OTP. " + remainingAttempts + " attempts remaining." :
                    "Maximum attempts reached. Please request a new OTP."
            );
            
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("OTP verification failed", errorData));
        }
    }
    
    @PostMapping("/resend")
    @Operation(summary = "Resend OTP", description = "Resend OTP to the same identifier")
    public ResponseEntity<ApiResponse> resendOTP(
            @RequestBody @Valid ResendOTPRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        if (!otpService.canResendOTP(request.getIdentifier(), request.getOtpType())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Cannot resend OTP yet. Please wait before requesting a new OTP."));
        }
        
        String sessionId = getSessionId(httpRequest);
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        OTPVerification otp = otpService.resendOTP(request.getIdentifier(), request.getOtpType());
        
        Map<String, Object> response = Map.of(
            "message", "OTP resent successfully to " + otpService.maskIdentifier(request.getIdentifier()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully", response));
    }
    
    @PostMapping("/generate/signup")
    @Operation(summary = "Generate signup OTP", description = "Generate OTP for email verification during signup")
    public ResponseEntity<ApiResponse> generateSignupOTP(
            @RequestBody @Valid SignupOTPRequest request,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        
        OTPVerification otp = otpService.generateSignupOTP(request.getEmail(), sessionId);
        
        Map<String, Object> response = Map.of(
            "message", "Verification OTP sent to " + otpService.maskIdentifier(request.getEmail()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Signup OTP generated successfully", response));
    }
    
    @PostMapping("/generate/password-reset")
    @Operation(summary = "Generate password reset OTP", description = "Generate OTP for password reset")
    public ResponseEntity<ApiResponse> generatePasswordResetOTP(
            @RequestBody @Valid PasswordResetOTPRequest request) {
        
        OTPVerification otp = otpService.generatePasswordResetOTP(request.getEmail());
        
        Map<String, Object> response = Map.of(
            "message", "Password reset OTP sent to " + otpService.maskIdentifier(request.getEmail()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP generated successfully", response));
    }
    
    @PostMapping("/generate/order-confirmation")
    @Operation(summary = "Generate order confirmation OTP", description = "Generate OTP for order confirmation")
    public ResponseEntity<ApiResponse> generateOrderConfirmationOTP(
            @RequestBody @Valid OrderConfirmationOTPRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        
        OTPVerification otp = otpService.generateOrderConfirmationOTP(
            request.getIdentifier(), userId, sessionId);
        
        Map<String, Object> response = Map.of(
            "message", "Order confirmation OTP sent to " + otpService.maskIdentifier(request.getIdentifier()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Order confirmation OTP generated successfully", response));
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get OTP status", description = "Check if there's an active OTP for identifier and type")
    public ResponseEntity<ApiResponse> getOTPStatus(
            @RequestParam String identifier,
            @RequestParam OTPVerification.OTPType otpType) {
        
        boolean hasActiveOTP = otpService.hasActiveOTP(identifier, otpType);
        int remainingAttempts = otpService.getRemainingOTPAttempts(identifier, otpType);
        boolean canResend = otpService.canResendOTP(identifier, otpType);
        
        Map<String, Object> status = Map.of(
            "hasActiveOTP", hasActiveOTP,
            "remainingAttempts", remainingAttempts,
            "canResend", canResend,
            "maskedIdentifier", otpService.maskIdentifier(identifier)
        );
        
        return ResponseEntity.ok(ApiResponse.success("OTP status retrieved successfully", status));
    }
    
    // Helper method to get session ID
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }
    
    // DTOs
    public static class GenerateOTPRequest {
        @NotBlank(message = "Identifier is required")
        private String identifier;
        
        private OTPVerification.OTPType otpType;
        
        // Getters and setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public OTPVerification.OTPType getOtpType() { return otpType; }
        public void setOtpType(OTPVerification.OTPType otpType) { this.otpType = otpType; }
    }
    
    public static class VerifyOTPRequest {
        @NotBlank(message = "Identifier is required")
        private String identifier;
        
        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "\\d{4,6}", message = "OTP must be 4-6 digits")
        private String otpCode;
        
        private OTPVerification.OTPType otpType;
        
        // Getters and setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
        public OTPVerification.OTPType getOtpType() { return otpType; }
        public void setOtpType(OTPVerification.OTPType otpType) { this.otpType = otpType; }
    }
    
    public static class ResendOTPRequest {
        @NotBlank(message = "Identifier is required")
        private String identifier;
        
        private OTPVerification.OTPType otpType;
        
        // Getters and setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public OTPVerification.OTPType getOtpType() { return otpType; }
        public void setOtpType(OTPVerification.OTPType otpType) { this.otpType = otpType; }
    }
    
    public static class SignupOTPRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class PasswordResetOTPRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class OrderConfirmationOTPRequest {
        @NotBlank(message = "Identifier is required")
        private String identifier;
        
        // Getters and setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }
}
