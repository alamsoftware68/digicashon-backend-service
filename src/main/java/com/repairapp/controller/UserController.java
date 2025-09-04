package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.Address;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.PaymentMethod;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.AuthService;
import com.repairapp.service.OTPService;
import com.repairapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management REST controller
 */
@Tag(name = "User Management", description = "User profile and data management APIs")
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private OTPService otpService;

    /**
     * Get current user profile
     */
    @Operation(summary = "Get user profile", description = "Get current user's profile information")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getUserProfile() {
        User currentUser = authService.getCurrentUser();
        currentUser.setPassword(null); // Remove password from response
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", currentUser));
    }

    /**
     * Update user profile
     */
    @Operation(summary = "Update user profile", description = "Update current user's profile information")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateUserProfile(@Valid @RequestBody User userDetails) {
        User currentUser = authService.getCurrentUser();
        User updatedUser = userService.updateUserProfile(currentUser.getId(), userDetails);
        updatedUser.setPassword(null); // Remove password from response
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    /**
     * Get user addresses
     */
    @Operation(summary = "Get user addresses", description = "Get all addresses for the current user")
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getUserAddresses() {
        User currentUser = authService.getCurrentUser();
        List<Address> addresses = userService.getUserAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    /**
     * Add new address
     */
    @Operation(summary = "Add new address", description = "Add a new address for the current user")
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Address>> addUserAddress(@Valid @RequestBody Address address) {
        User currentUser = authService.getCurrentUser();
        Address savedAddress = userService.addUserAddress(currentUser.getId(), address);
        return ResponseEntity.ok(ApiResponse.success("Address added successfully", savedAddress));
    }

    /**
     * Update address
     */
    @Operation(summary = "Update address", description = "Update an existing address")
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Address>> updateUserAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody Address addressDetails) {
        User currentUser = authService.getCurrentUser();
        Address updatedAddress = userService.updateUserAddress(currentUser.getId(), addressId, addressDetails);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updatedAddress));
    }

    /**
     * Delete address
     */
    @Operation(summary = "Delete address", description = "Delete an existing address")
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteUserAddress(@PathVariable Long addressId) {
        User currentUser = authService.getCurrentUser();
        userService.deleteUserAddress(currentUser.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }

    /**
     * Get user payment methods
     */
    @Operation(summary = "Get payment methods", description = "Get all payment methods for the current user")
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getUserPaymentMethods() {
        User currentUser = authService.getCurrentUser();
        List<PaymentMethod> paymentMethods = userService.getUserPaymentMethods(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Payment methods retrieved successfully", paymentMethods));
    }

    /**
     * Add new payment method
     */
    @Operation(summary = "Add payment method", description = "Add a new payment method for the current user")
    @PostMapping("/payment-methods")
    public ResponseEntity<ApiResponse<PaymentMethod>> addUserPaymentMethod(@Valid @RequestBody PaymentMethod paymentMethod) {
        User currentUser = authService.getCurrentUser();
        PaymentMethod savedPaymentMethod = userService.addUserPaymentMethod(currentUser.getId(), paymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Payment method added successfully", savedPaymentMethod));
    }

    /**
     * Delete payment method
     */
    @Operation(summary = "Delete payment method", description = "Delete an existing payment method")
    @DeleteMapping("/payment-methods/{paymentMethodId}")
    public ResponseEntity<ApiResponse<String>> deleteUserPaymentMethod(@PathVariable Long paymentMethodId) {
        User currentUser = authService.getCurrentUser();
        userService.deleteUserPaymentMethod(currentUser.getId(), paymentMethodId);
        return ResponseEntity.ok(ApiResponse.success("Payment method deleted successfully"));
    }

    /**
     * Request email change OTP
     */
    @Operation(summary = "Request email change OTP", description = "Generate OTP for email change verification")
    @PostMapping("/profile/change-email/request-otp")
    public ResponseEntity<ApiResponse> requestEmailChangeOTP(@RequestBody EmailChangeOTPRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Check if new email is already taken
        if (!authService.isEmailAvailable(request.getNewEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email address is already in use by another account"));
        }
        
        OTPVerification otp = otpService.generateEmailChangeOTP(request.getNewEmail(), currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.success(
            "Email change verification code sent to " + otpService.maskIdentifier(request.getNewEmail()),
            java.util.Map.of("expiresAt", otp.getExpiresAt())));
    }
    
    /**
     * Verify email change with OTP
     */
    @Operation(summary = "Change email with OTP", description = "Change email address after OTP verification")
    @PostMapping("/profile/change-email/verify")
    public ResponseEntity<ApiResponse> changeEmailWithOTP(@Valid @RequestBody ChangeEmailWithOTPRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Verify OTP
        boolean otpValid = otpService.verifyEmailOTP(
            request.getNewEmail(),
            request.getOtpCode(),
            OTPVerification.OTPType.EMAIL_CHANGE_VERIFICATION);
        
        if (!otpValid) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid or expired OTP"));
        }
        
        // Update email
        User userDetails = new User();
        userDetails.setEmail(request.getNewEmail());
        User updatedUser = userService.updateUserProfile(currentUser.getId(), userDetails);
        updatedUser.setPassword(null);
        
        return ResponseEntity.ok(ApiResponse.success("Email address changed successfully", updatedUser));
    }
    
    /**
     * Set address as default
     */
    @Operation(summary = "Set default address", description = "Set an address as the default address")
    @PostMapping("/addresses/{addressId}/set-default")
    public ResponseEntity<ApiResponse> setDefaultAddress(@PathVariable Long addressId) {
        User currentUser = authService.getCurrentUser();
        Address address = userService.setDefaultAddress(currentUser.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated successfully", address));
    }
    
    /**
     * Get default address
     */
    @Operation(summary = "Get default address", description = "Get the user's default address")
    @GetMapping("/addresses/default")
    public ResponseEntity<ApiResponse> getDefaultAddress() {
        User currentUser = authService.getCurrentUser();
        Address defaultAddress = userService.getDefaultAddress(currentUser.getId());
        
        if (defaultAddress == null) {
            return ResponseEntity.ok(ApiResponse.success("No default address found", null));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", defaultAddress));
    }

    /**
     * Get available technicians (for admin/customer reference)
     */
    @Operation(summary = "Get technicians", description = "Get list of available technicians")
    @GetMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<User>>> getAvailableTechnicians() {
        List<User> technicians = userService.getAvailableTechnicians();
        // Remove passwords from response
        technicians.forEach(tech -> tech.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success("Technicians retrieved successfully", technicians));
    }
    
    // DTOs
    public static class EmailChangeOTPRequest {
        @jakarta.validation.constraints.Email(message = "Valid email is required")
        @jakarta.validation.constraints.NotBlank(message = "New email is required")
        private String newEmail;
        
        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    }
    
    public static class ChangeEmailWithOTPRequest {
        @jakarta.validation.constraints.Email(message = "Valid email is required")
        @jakarta.validation.constraints.NotBlank(message = "New email is required")
        private String newEmail;
        
        @jakarta.validation.constraints.NotBlank(message = "OTP code is required")
        @jakarta.validation.constraints.Pattern(regexp = "\\d{4,6}", message = "OTP must be 4-6 digits")
        private String otpCode;
        
        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    }
}
