package com.repairapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_phone", columnList = "phone"),
    @Index(name = "idx_otp_type_identifier", columnList = "otp_type, identifier")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTPVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "identifier", nullable = false, length = 100)
    @NotBlank
    private String identifier; // Email or phone number
    
    @Column(name = "email", length = 100)
    @Email
    private String email;
    
    @Column(name = "phone", length = 15)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 30)
    @NotNull
    private OTPType otpType;
    
    @Column(name = "otp_code", nullable = false, length = 6)
    @NotBlank
    private String otpCode;
    
    @Column(name = "is_verified", nullable = false)
    @NotNull
    private Boolean isVerified = false;
    
    @Column(name = "attempt_count", nullable = false)
    @NotNull
    private Integer attemptCount = 0;
    
    @Column(name = "max_attempts", nullable = false)
    @NotNull
    private Integer maxAttempts = 5;
    
    @Column(name = "expires_at", nullable = false)
    @NotNull
    private LocalDateTime expiresAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "session_id", length = 255)
    private String sessionId;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata for additional context
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Version
    private Long version;
    
    public enum OTPType {
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION, 
        PASSWORD_RESET,
        ORDER_CONFIRMATION,
        ACCOUNT_RECOVERY,
        TWO_FACTOR_AUTH,
        EMAIL_CHANGE_VERIFICATION
    }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isMaxAttemptsReached() {
        return attemptCount >= maxAttempts;
    }
    
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    public boolean canAttempt() {
        return !isExpired() && !isMaxAttemptsReached() && !isVerified;
    }
    
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public boolean isValidCode(String inputCode) {
        return this.otpCode != null && this.otpCode.equals(inputCode);
    }
    
    // Check if OTP matches email or phone
    public boolean matchesIdentifier(String identifier) {
        return this.identifier != null && this.identifier.equalsIgnoreCase(identifier);
    }
}
