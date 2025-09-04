package com.repairapp.controller;

import com.repairapp.dto.auth.*;
import com.repairapp.dto.auth.JwtResponse;
import com.repairapp.dto.auth.LoginRequest;
import com.repairapp.dto.auth.SignupRequest;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.AuthService;
import com.repairapp.service.CartService;
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

@Tag(name = "Authentication", description = "Authentication management APIs with OTP verification")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OTPService otpService;
    private final CartService cartService;

    @Operation(summary = "User login with password", description = "Authenticate user with mobile number and password, return JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest) {
        
        String mobile = loginRequest.getMobile();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-LOGIN] Password login attempt for mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            log.info("[AUTH-LOGIN] Password login successful for mobile: {}, user ID: {}, role: {}", 
                maskMobile(mobile), jwtResponse.getId(), jwtResponse.getRole());
            
            // Merge session cart with user cart after login
            try {
                User user = authService.getUserByMobileOrEmail(mobile);
                cartService.mergeSessionCartWithUserCart(sessionId, user);
                log.debug("[AUTH-LOGIN] Cart merge completed for user ID: {} from session: {}", 
                    user.getId(), sessionId);
            } catch (Exception e) {
                log.warn("[AUTH-LOGIN] Failed to merge carts after login for mobile: {}, session: {}", 
                    maskMobile(mobile), sessionId, e);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
            
        } catch (Exception e) {
            log.error("[AUTH-LOGIN] Password login failed for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }

    @Operation(summary = "Step 1: Send OTP for login", description = "Send OTP to mobile number for login")
    @PostMapping("/login/send-otp")
    public ResponseEntity<ApiResponse> sendLoginOTP(
            @Valid @RequestBody MobileSignupRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-OTP-LOGIN] Send OTP request for mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            // Check if mobile is registered
            User user = authService.getUserByMobile(mobile);
            if (user == null) {
                log.warn("[AUTH-OTP-LOGIN] Login OTP request failed - mobile not registered: {}", 
                    maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number is not registered"));
            }
            
            log.debug("[AUTH-OTP-LOGIN] Found registered user ID: {} for mobile: {}", 
                user.getId(), maskMobile(mobile));
            
            // Generate OTP for login
            OTPVerification otp = otpService.generatePhoneOTP(
                mobile, 
                OTPVerification.OTPType.TWO_FACTOR_AUTH, 
                user.getId(), 
                sessionId);
            
            log.info("[AUTH-OTP-LOGIN] OTP generated successfully for mobile: {}, OTP ID: {}, expires at: {}", 
                maskMobile(mobile), otp.getId(), otp.getExpiresAt());
            
            Map<String, Object> response = Map.of(
                "message", "OTP sent to " + otpService.maskIdentifier(mobile),
                "maskedMobile", otpService.maskIdentifier(mobile),
                "expiresAt", otp.getExpiresAt(),
                "nextStep", "verify-login-otp"
            );
            
            return ResponseEntity.ok(ApiResponse.success("Login OTP sent to mobile number", response));
            
        } catch (Exception e) {
            log.error("[AUTH-OTP-LOGIN] Failed to send login OTP for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }
    
    @Operation(summary = "Step 2: Login with OTP", description = "Verify OTP and login user")
    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse> loginWithOTP(
            @Valid @RequestBody OTPVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String otpCode = request.getOtpCode();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-OTP-VERIFY] OTP verification attempt for mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            // Verify OTP
            boolean otpValid = otpService.verifyPhoneOTP(
                mobile, 
                otpCode, 
                OTPVerification.OTPType.TWO_FACTOR_AUTH);
            
            if (!otpValid) {
                int remainingAttempts = otpService.getRemainingOTPAttempts(
                    mobile, OTPVerification.OTPType.TWO_FACTOR_AUTH);
                
                log.warn("[AUTH-OTP-VERIFY] Invalid OTP for mobile: {}, remaining attempts: {}", 
                    maskMobile(mobile), remainingAttempts);
                
                Map<String, Object> errorData = Map.of(
                    "remainingAttempts", remainingAttempts,
                    "canResend", otpService.canResendOTP(mobile, OTPVerification.OTPType.TWO_FACTOR_AUTH)
                );
                
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid OTP. Please try again.", errorData));
            }
            
            log.info("[AUTH-OTP-VERIFY] OTP verification successful for mobile: {}", maskMobile(mobile));
            
            // Get user and generate JWT tokens
            User user = authService.getUserByMobile(mobile);
            if (user == null) {
                log.error("[AUTH-OTP-VERIFY] User not found after successful OTP for mobile: {}", 
                    maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found"));
            }
            
            log.debug("[AUTH-OTP-VERIFY] Found user ID: {} for mobile: {}, role: {}", 
                user.getId(), maskMobile(mobile), user.getRole());
            
            // Generate JWT tokens for successful OTP login
            JwtResponse jwtResponse = authService.authenticateUserWithoutPassword(user);
            
            log.info("[AUTH-OTP-VERIFY] JWT tokens generated for OTP login, user ID: {}, role: {}", 
                jwtResponse.getId(), jwtResponse.getRole());
            
            // Merge session cart with user cart after login
            try {
                cartService.mergeSessionCartWithUserCart(sessionId, user);
                log.debug("[AUTH-OTP-VERIFY] Cart merge completed for user ID: {} from session: {}", 
                    user.getId(), sessionId);
            } catch (Exception e) {
                log.warn("[AUTH-OTP-VERIFY] Failed to merge carts after OTP login for mobile: {}, session: {}", 
                    maskMobile(mobile), sessionId, e);
            }
            
            return ResponseEntity.ok(ApiResponse.success("OTP login successful", jwtResponse));
            
        } catch (Exception e) {
            log.error("[AUTH-OTP-VERIFY] OTP login failed for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }
    
    @Operation(summary = "Resend login OTP", description = "Resend OTP to mobile number for login")
    @PostMapping("/login/resend-otp")
    public ResponseEntity<ApiResponse> resendLoginOTP(
            @Valid @RequestBody MobileSignupRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-OTP-RESEND] Resend login OTP request for mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            // Check if mobile is registered
            User user = authService.getUserByMobile(mobile);
            if (user == null) {
                log.warn("[AUTH-OTP-RESEND] Resend OTP failed - mobile not registered: {}", 
                    maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number is not registered"));
            }
            
            log.debug("[AUTH-OTP-RESEND] Found registered user ID: {} for mobile: {}", 
                user.getId(), maskMobile(mobile));
            
            // Check if we can resend OTP
            if (!otpService.canResendOTP(mobile, OTPVerification.OTPType.TWO_FACTOR_AUTH)) {
                log.warn("[AUTH-OTP-RESEND] Rate limit exceeded for mobile: {}", maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please wait before requesting a new OTP"));
            }
            
            // Resend OTP
            OTPVerification otp = otpService.resendOTP(mobile, OTPVerification.OTPType.TWO_FACTOR_AUTH);
            
            log.info("[AUTH-OTP-RESEND] Login OTP resent successfully for mobile: {}, OTP ID: {}, expires at: {}", 
                maskMobile(mobile), otp.getId(), otp.getExpiresAt());
            
            Map<String, Object> response = Map.of(
                "message", "OTP resent to " + otpService.maskIdentifier(mobile),
                "maskedMobile", otpService.maskIdentifier(mobile),
                "expiresAt", otp.getExpiresAt()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Login OTP resent successfully", response));
            
        } catch (Exception e) {
            log.error("[AUTH-OTP-RESEND] Failed to resend login OTP for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }

    // ========== THREE-STEP SIGNUP PROCESS ==========
    
    @Operation(summary = "Step 1: Initiate signup with mobile", description = "Start signup process by providing mobile number")
    @PostMapping("/signup/mobile")
    public ResponseEntity<ApiResponse> initiateSignupWithMobile(
            @Valid @RequestBody MobileSignupRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-SIGNUP-MOBILE] Signup initiation request for mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            // Check if mobile is already registered
            if (!authService.isMobileAvailable(mobile)) {
                log.warn("[AUTH-SIGNUP-MOBILE] Signup failed - mobile already registered: {}", 
                    maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number is already registered"));
            }
            
            log.debug("[AUTH-SIGNUP-MOBILE] Mobile number available for registration: {}", 
                maskMobile(mobile));
            
            // Generate OTP for mobile
            OTPVerification otp = otpService.generateMobileSignupOTP(mobile, sessionId);
            
            log.info("[AUTH-SIGNUP-MOBILE] Signup OTP generated successfully for mobile: {}, OTP ID: {}, expires at: {}", 
                maskMobile(mobile), otp.getId(), otp.getExpiresAt());
            
            Map<String, Object> response = Map.of(
                "message", "OTP sent to " + otpService.maskIdentifier(mobile),
                "maskedMobile", otpService.maskIdentifier(mobile),
                "expiresAt", otp.getExpiresAt(),
                "nextStep", "verify-otp"
            );
            
            return ResponseEntity.ok(ApiResponse.success("OTP sent to mobile number", response));
            
        } catch (Exception e) {
            log.error("[AUTH-SIGNUP-MOBILE] Failed to initiate signup for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }
    
    @Operation(summary = "Step 2: Verify mobile OTP", description = "Verify OTP sent to mobile number")
    @PostMapping("/signup/verify-otp")
    public ResponseEntity<ApiResponse> verifySignupOTP(
            @Valid @RequestBody OTPVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String otpCode = request.getOtpCode();
        String sessionId = getSessionId(httpRequest);
        
        log.info("[AUTH-SIGNUP-VERIFY] OTP verification attempt for signup mobile: {} from session: {}", 
            maskMobile(mobile), sessionId);
        
        try {
            // Verify OTP
            boolean otpValid = otpService.verifyPhoneOTP(
                mobile, 
                otpCode, 
                OTPVerification.OTPType.PHONE_VERIFICATION);
            
            if (!otpValid) {
                int remainingAttempts = otpService.getRemainingOTPAttempts(
                    mobile, OTPVerification.OTPType.PHONE_VERIFICATION);
                
                log.warn("[AUTH-SIGNUP-VERIFY] Invalid OTP for signup mobile: {}, remaining attempts: {}", 
                    maskMobile(mobile), remainingAttempts);
                
                Map<String, Object> errorData = Map.of(
                    "remainingAttempts", remainingAttempts,
                    "canResend", otpService.canResendOTP(mobile, OTPVerification.OTPType.PHONE_VERIFICATION)
                );
                
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid OTP. Please try again.", errorData));
            }
            
            log.info("[AUTH-SIGNUP-VERIFY] Signup OTP verification successful for mobile: {}", 
                maskMobile(mobile));
            
            // Store mobile in session for next step
            HttpSession session = httpRequest.getSession();
            session.setAttribute("verified_mobile", mobile);
            session.setAttribute("otp_verified_at", System.currentTimeMillis());
            
            log.debug("[AUTH-SIGNUP-VERIFY] Mobile verification stored in session: {} for mobile: {}", 
                sessionId, maskMobile(mobile));
            
            Map<String, Object> response = Map.of(
                "message", "Mobile number verified successfully",
                "mobile", mobile,
                "nextStep", "complete-registration"
            );
            
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
            
        } catch (Exception e) {
            log.error("[AUTH-SIGNUP-VERIFY] Signup OTP verification failed for mobile: {} from session: {}", 
                maskMobile(mobile), sessionId, e);
            throw e;
        }
    }
    
    @Operation(summary = "Step 3: Complete registration", description = "Complete user registration with full details")
    @PostMapping("/signup/complete")
    public ResponseEntity<ApiResponse> completeSignup(
            @Valid @RequestBody CompleteSignupRequest request,
            HttpServletRequest httpRequest) {
        
        String mobile = request.getMobile();
        String email = request.getEmail();
        String name = request.getName();
        String city = request.getCity();
        
        log.info("[AUTH-SIGNUP-COMPLETE] Complete registration request for mobile: {}, email: {}, name: {}, city: {}", 
            maskMobile(mobile), maskEmail(email), name, city);
        
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                log.error("[AUTH-SIGNUP-COMPLETE] Invalid session - no session found for mobile: {}", 
                    maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid session. Please start signup process again."));
            }
            
            String verifiedMobile = (String) session.getAttribute("verified_mobile");
            Long otpVerifiedAt = (Long) session.getAttribute("otp_verified_at");
            
            log.debug("[AUTH-SIGNUP-COMPLETE] Session verification - expected: {}, provided: {}, session: {}", 
                maskMobile(verifiedMobile), maskMobile(mobile), session.getId());
            
            // Validate session data
            if (verifiedMobile == null || !verifiedMobile.equals(mobile)) {
                log.error("[AUTH-SIGNUP-COMPLETE] Mobile mismatch - session mobile: {}, request mobile: {}", 
                    maskMobile(verifiedMobile), maskMobile(mobile));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number mismatch. Please start signup process again."));
            }
            
            // Check if OTP verification is still valid (within 30 minutes)
            if (otpVerifiedAt == null || (System.currentTimeMillis() - otpVerifiedAt) > 30 * 60 * 1000) {
                log.error("[AUTH-SIGNUP-COMPLETE] OTP verification expired for mobile: {}, verified at: {}", 
                    maskMobile(mobile), otpVerifiedAt);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("OTP verification expired. Please start signup process again."));
            }
            
            // Check if email is available
            if (!authService.isEmailAvailable(email)) {
                log.warn("[AUTH-SIGNUP-COMPLETE] Email already registered: {}", maskEmail(email));
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email address is already registered"));
            }
            
            log.debug("[AUTH-SIGNUP-COMPLETE] Email available for registration: {}", maskEmail(email));
            
            // Create user
            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setName(name);
            signupRequest.setEmail(email);
            signupRequest.setPassword(request.getPassword());
            signupRequest.setPhone(mobile);
            
            User user = authService.registerUser(signupRequest);
            
            log.info("[AUTH-SIGNUP-COMPLETE] User registered successfully - ID: {}, mobile: {}, email: {}", 
                user.getId(), maskMobile(mobile), maskEmail(email));
            
            // Set location fields from request
            user.setCity(city);
            // Save user with location details
            user = authService.saveUser(user);
            
            log.debug("[AUTH-SIGNUP-COMPLETE] User location details saved for user ID: {}, city: {}", 
                user.getId(), city);
            
            // Clear session data
            session.removeAttribute("verified_mobile");
            session.removeAttribute("otp_verified_at");
            
            log.debug("[AUTH-SIGNUP-COMPLETE] Session data cleared for mobile: {}", maskMobile(mobile));
            
            // Generate JWT token for automatic login
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setMobile(mobile);
            loginRequest.setPassword(request.getPassword());
            
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            
            log.info("[AUTH-SIGNUP-COMPLETE] Auto-login successful after registration - user ID: {}, role: {}", 
                jwtResponse.getId(), jwtResponse.getRole());
            
            Map<String, Object> response = Map.of(
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "mobile", user.getPhone(),
                    "city", user.getCity()
                ),
                "tokens", jwtResponse
            );
            
            return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", response));
            
        } catch (Exception e) {
            log.error("[AUTH-SIGNUP-COMPLETE] Complete registration failed for mobile: {}, email: {}", 
                maskMobile(mobile), maskEmail(email), e);
            throw e;
        }
    }

    @Operation(summary = "Request password reset OTP", description = "Send OTP to email for password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        
        // Check if user exists with this email
        User user = authService.getUserByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("No account found with this email address."));
        }
        
        OTPVerification otp = otpService.generatePasswordResetOTP(request.getEmail());
        
        Map<String, Object> response = Map.of(
            "message", "Password reset OTP sent to " + otpService.maskIdentifier(request.getEmail()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent successfully", response));
    }

    @Operation(summary = "Reset password with OTP", description = "Reset password after OTP verification")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        
        // Verify OTP
        boolean otpValid = otpService.verifyEmailOTP(
            request.getEmail(), 
            request.getOtpCode(), 
            OTPVerification.OTPType.PASSWORD_RESET);
        
        if (!otpValid) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid or expired OTP"));
        }
        
        // Reset password
        boolean success = authService.resetPasswordWithEmail(request.getEmail(), request.getNewPassword());
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to reset password"));
        }
    }

    @Operation(summary = "Change password", description = "Change user password with current password verification")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Authentication required"));
        }
        
        boolean success = authService.changePassword(
            userPrincipal.getUsername(), 
            request.getOldPassword(), 
            request.getNewPassword());
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid current password"));
        }
    }

    @Operation(summary = "Refresh JWT token", description = "Generate new access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestParam String refreshToken) {
        JwtResponse jwtResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", jwtResponse));
    }

    @Operation(summary = "Get current user", description = "Get currently authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Authentication required"));
        }
        
        // Create a user response object from UserPrincipal
        Map<String, Object> userInfo = Map.of(
            "id", userPrincipal.getId(),
            "phone", userPrincipal.getPhone(),
            "email", userPrincipal.getEmail(),
            "name", userPrincipal.getName(),
            "role", userPrincipal.getRole(),
            "isActive", userPrincipal.getIsActive()
        );
        
        return ResponseEntity.ok(ApiResponse.success("User information retrieved", userInfo));
    }

    @Operation(summary = "User logout", description = "Logout user (client should remove token)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @Operation(summary = "Check email availability", description = "Check if email is already registered")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        
        Map<String, Object> response = Map.of(
            "email", email,
            "isAvailable", isAvailable
        );
        
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", response));
    }


    @Operation(summary = "Resend signup OTP", description = "Resend OTP to mobile number during signup")
    @PostMapping("/signup/resend-otp")
    public ResponseEntity<ApiResponse> resendSignupOTP(
            @Valid @RequestBody MobileSignupRequest request,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        
        // Check if we can resend OTP
        if (!otpService.canResendOTP(request.getMobile(), OTPVerification.OTPType.PHONE_VERIFICATION)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Please wait before requesting a new OTP"));
        }
        
        // Resend OTP
        OTPVerification otp = otpService.resendOTP(request.getMobile(), OTPVerification.OTPType.PHONE_VERIFICATION);
        
        Map<String, Object> response = Map.of(
            "message", "OTP resent to " + otpService.maskIdentifier(request.getMobile()),
            "maskedMobile", otpService.maskIdentifier(request.getMobile()),
            "expiresAt", otp.getExpiresAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully", response));
    }

    // Helper method to get session ID
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }
    
    // Helper method to mask mobile number for logging
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() <= 3) {
            return "***";
        }
        if (mobile.length() <= 6) {
            return mobile.substring(0, 2) + "***";
        }
        return mobile.substring(0, 2) + "***" + mobile.substring(mobile.length() - 2);
    }
    
    // Helper method to mask email for logging
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domain;
        }
        return localPart.substring(0, 2) + "***@" + domain;
    }
    

    // DTOs
    public static class EnhancedSignupRequest extends SignupRequest {
        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "\\d{4,6}", message = "OTP must be 4-6 digits")
        private String otpCode;

        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    }

    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "\\d{4,6}", message = "OTP must be 4-6 digits")
        private String otpCode;

        @NotBlank(message = "New password is required")
        private String newPassword;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String oldPassword;

        @NotBlank(message = "New password is required")
        private String newPassword;

        // Getters and setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
