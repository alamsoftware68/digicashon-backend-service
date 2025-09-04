package com.repairapp.service;

import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.User;

import java.util.Optional;

public interface OTPService {
    
    // OTP Generation
    OTPVerification generateOTP(String identifier, OTPVerification.OTPType otpType, Long userId, String sessionId);
    OTPVerification generateEmailOTP(String email, OTPVerification.OTPType otpType, Long userId, String sessionId);
    OTPVerification generatePhoneOTP(String phone, OTPVerification.OTPType otpType, Long userId, String sessionId);
    
    // OTP Verification
    boolean verifyOTP(String identifier, String otpCode, OTPVerification.OTPType otpType);
    boolean verifyEmailOTP(String email, String otpCode, OTPVerification.OTPType otpType);
    boolean verifyPhoneOTP(String phone, String otpCode, OTPVerification.OTPType otpType);
    OTPVerification verifyAndGetOTP(String identifier, String otpCode, OTPVerification.OTPType otpType);
    
    // OTP Management
    Optional<OTPVerification> findActiveOTP(String identifier, OTPVerification.OTPType otpType);
    boolean hasActiveOTP(String identifier, OTPVerification.OTPType otpType);
    void invalidateOTP(Long otpId);
    void invalidateAllActiveOTPs(String identifier, OTPVerification.OTPType otpType);
    
    // Rate Limiting
    boolean canGenerateOTP(String identifier, OTPVerification.OTPType otpType);
    int getRemainingOTPAttempts(String identifier, OTPVerification.OTPType otpType);
    boolean hasReachedRateLimit(String identifier, OTPVerification.OTPType otpType);
    
    // Resend OTP
    OTPVerification resendOTP(String identifier, OTPVerification.OTPType otpType);
    boolean canResendOTP(String identifier, OTPVerification.OTPType otpType);
    
    // Cleanup Operations
    void cleanupExpiredOTPs();
    void cleanupVerifiedOTPs(int daysOld);
    
    // Validation
    void validateIdentifier(String identifier, OTPVerification.OTPType otpType);
    void validateOTPCode(String otpCode);
    boolean isValidIdentifierFormat(String identifier, OTPVerification.OTPType otpType);
    
    // Utility Methods
    String generateOTPCode();
    String maskIdentifier(String identifier);
    boolean isExpired(OTPVerification otp);
    
    // Specific OTP Flows
    OTPVerification generateSignupOTP(String email, String sessionId);
    OTPVerification generateMobileSignupOTP(String mobile, String sessionId);
    OTPVerification generatePasswordResetOTP(String email);
    OTPVerification generateOrderConfirmationOTP(String identifier, Long userId, String sessionId);
    OTPVerification generateEmailChangeOTP(String newEmail, Long userId);
}
