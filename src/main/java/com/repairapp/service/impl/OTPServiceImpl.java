package com.repairapp.service.impl;

import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.User;
import com.repairapp.exception.BadRequestException;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.OTPVerificationRepository;
import com.repairapp.service.OTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OTPServiceImpl implements OTPService {
    
    private final OTPVerificationRepository otpRepository;
    private final com.repairapp.service.NotificationService notificationService;
    
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_GENERATION_PER_HOUR = 5;
    private static final int RESEND_COOLDOWN_MINUTES = 2;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Email pattern for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Phone pattern for validation
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    @Override
    public OTPVerification generateOTP(String identifier, OTPVerification.OTPType otpType, Long userId, String sessionId) {
        validateIdentifier(identifier, otpType);
        
        if (!canGenerateOTP(identifier, otpType)) {
            throw new BadRequestException("Rate limit exceeded. Please try again later.");
        }
        
        // Invalidate any existing active OTPs
        invalidateAllActiveOTPs(identifier, otpType);
        
        OTPVerification otp = new OTPVerification();
        otp.setIdentifier(identifier.toLowerCase().trim());
        otp.setOtpType(otpType);
        otp.setOtpCode(generateOTPCode());
        otp.setUserId(userId);
        otp.setSessionId(sessionId);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        
        // Set email or phone based on identifier format
        if (isEmail(identifier)) {
            otp.setEmail(identifier.toLowerCase().trim());
        } else if (isPhone(identifier)) {
            otp.setPhone(identifier.trim());
        }
        
        OTPVerification savedOTP = otpRepository.save(otp);
        log.info("Generated OTP for identifier: {} of type: {}", maskIdentifier(identifier), otpType);
        
        // Send notification
        try {
            notificationService.sendOTPNotification(savedOTP);
        } catch (Exception e) {
            log.error("Failed to send OTP notification for identifier: {}", maskIdentifier(identifier), e);
        }
        
        return savedOTP;
    }
    
    @Override
    public OTPVerification generateEmailOTP(String email, OTPVerification.OTPType otpType, Long userId, String sessionId) {
        if (!isEmail(email)) {
            throw new BadRequestException("Invalid email format");
        }
        return generateOTP(email, otpType, userId, sessionId);
    }
    
    @Override
    public OTPVerification generatePhoneOTP(String phone, OTPVerification.OTPType otpType, Long userId, String sessionId) {
        if (!isPhone(phone)) {
            throw new BadRequestException("Invalid phone number format");
        }
        return generateOTP(phone, otpType, userId, sessionId);
    }
    
    @Override
    public boolean verifyOTP(String identifier, String otpCode, OTPVerification.OTPType otpType) {
        validateIdentifier(identifier, otpType);
        validateOTPCode(otpCode);
        
        Optional<OTPVerification> otpOpt = findActiveOTP(identifier, otpType);
        
        if (otpOpt.isEmpty()) {
            log.warn("No active OTP found for identifier: {} and type: {}", maskIdentifier(identifier), otpType);
            return false;
        }
        
        OTPVerification otp = otpOpt.get();
        
        if (!otp.canAttempt()) {
            log.warn("OTP verification failed - cannot attempt for identifier: {}", maskIdentifier(identifier));
            return false;
        }
        
        otp.incrementAttempt();
        
        if (otp.isValidCode(otpCode)) {
            otp.markAsVerified();
            otpRepository.save(otp);
            log.info("OTP verified successfully for identifier: {} of type: {}", maskIdentifier(identifier), otpType);
            return true;
        } else {
            otpRepository.save(otp);
            log.warn("Invalid OTP code provided for identifier: {} of type: {}", maskIdentifier(identifier), otpType);
            return false;
        }
    }
    
    @Override
    public boolean verifyEmailOTP(String email, String otpCode, OTPVerification.OTPType otpType) {
        return verifyOTP(email, otpCode, otpType);
    }
    
    @Override
    public boolean verifyPhoneOTP(String phone, String otpCode, OTPVerification.OTPType otpType) {
        return verifyOTP(phone, otpCode, otpType);
    }
    
    @Override
    public OTPVerification verifyAndGetOTP(String identifier, String otpCode, OTPVerification.OTPType otpType) {
        if (verifyOTP(identifier, otpCode, otpType)) {
            return findActiveOTP(identifier, otpType)
                .orElseThrow(() -> new ResourceNotFoundException("Verified OTP not found"));
        }
        throw new BadRequestException("OTP verification failed");
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<OTPVerification> findActiveOTP(String identifier, OTPVerification.OTPType otpType) {
        return otpRepository.findActiveOTPByIdentifierAndType(identifier.toLowerCase().trim(), otpType, LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveOTP(String identifier, OTPVerification.OTPType otpType) {
        return findActiveOTP(identifier, otpType).isPresent();
    }
    
    @Override
    public void invalidateOTP(Long otpId) {
        OTPVerification otp = otpRepository.findById(otpId)
            .orElseThrow(() -> new ResourceNotFoundException("OTP not found with ID: " + otpId));
        
        otp.setIsVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);
        
        log.info("Invalidated OTP with ID: {}", otpId);
    }
    
    @Override
    public void invalidateAllActiveOTPs(String identifier, OTPVerification.OTPType otpType) {
        otpRepository.invalidateActiveOTPs(identifier.toLowerCase().trim(), otpType, LocalDateTime.now());
        log.info("Invalidated all active OTPs for identifier: {} of type: {}", maskIdentifier(identifier), otpType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canGenerateOTP(String identifier, OTPVerification.OTPType otpType) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = otpRepository.countOTPAttemptsByIdentifierAndTypeWithinPeriod(
            identifier.toLowerCase().trim(), otpType, oneHourAgo);
        
        return recentAttempts < MAX_OTP_GENERATION_PER_HOUR;
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getRemainingOTPAttempts(String identifier, OTPVerification.OTPType otpType) {
        Optional<OTPVerification> activeOTP = findActiveOTP(identifier, otpType);
        if (activeOTP.isEmpty()) {
            return 5; // Default max attempts
        }
        
        OTPVerification otp = activeOTP.get();
        return Math.max(0, otp.getMaxAttempts() - otp.getAttemptCount());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedRateLimit(String identifier, OTPVerification.OTPType otpType) {
        return !canGenerateOTP(identifier, otpType);
    }
    
    @Override
    public OTPVerification resendOTP(String identifier, OTPVerification.OTPType otpType) {
        if (!canResendOTP(identifier, otpType)) {
            throw new BadRequestException("Cannot resend OTP yet. Please wait before requesting a new OTP.");
        }
        
        // Get the latest OTP to preserve user and session info
        Optional<OTPVerification> latestOTP = otpRepository.findTopByIdentifierAndOtpTypeOrderByCreatedAtDesc(
            identifier.toLowerCase().trim(), otpType);
        
        Long userId = null;
        String sessionId = null;
        
        if (latestOTP.isPresent()) {
            userId = latestOTP.get().getUserId();
            sessionId = latestOTP.get().getSessionId();
        }
        
        return generateOTP(identifier, otpType, userId, sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canResendOTP(String identifier, OTPVerification.OTPType otpType) {
        Optional<OTPVerification> latestOTP = otpRepository.findTopByIdentifierAndOtpTypeOrderByCreatedAtDesc(
            identifier.toLowerCase().trim(), otpType);
        
        if (latestOTP.isEmpty()) {
            return true;
        }
        
        LocalDateTime lastCreated = latestOTP.get().getCreatedAt();
        LocalDateTime cooldownEnd = lastCreated.plusMinutes(RESEND_COOLDOWN_MINUTES);
        
        return LocalDateTime.now().isAfter(cooldownEnd);
    }
    
    @Override
    public void cleanupExpiredOTPs() {
        LocalDateTime currentTime = LocalDateTime.now();
        int deletedCount = otpRepository.deleteExpiredOTPs(currentTime);
        log.info("Cleaned up {} expired OTPs", deletedCount);
    }
    
    @Override
    public void cleanupVerifiedOTPs(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = otpRepository.deleteOldVerifiedOTPs(cutoffTime);
        log.info("Cleaned up {} verified OTPs older than {} days", deletedCount, daysOld);
    }
    
    @Override
    public void validateIdentifier(String identifier, OTPVerification.OTPType otpType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BadRequestException("Identifier cannot be empty");
        }
        
        if (!isValidIdentifierFormat(identifier, otpType)) {
            throw new BadRequestException("Invalid identifier format for OTP type: " + otpType);
        }
    }
    
    @Override
    public void validateOTPCode(String otpCode) {
        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new BadRequestException("OTP code cannot be empty");
        }
        
        if (!otpCode.matches("\\d{4,6}")) {
            throw new BadRequestException("OTP code must be 4-6 digits");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isValidIdentifierFormat(String identifier, OTPVerification.OTPType otpType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = identifier.trim();
        
        return switch (otpType) {
            case EMAIL_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE_VERIFICATION -> isEmail(trimmed);
            case PHONE_VERIFICATION -> isPhone(trimmed);
            case ORDER_CONFIRMATION, ACCOUNT_RECOVERY, TWO_FACTOR_AUTH -> isPhone(trimmed); // Mobile only for signup
        };
    }
    
    @Override
    public String generateOTPCode() {
        // For development/demo purposes, use hardcoded OTP
        String demoOtp = System.getProperty("demo.otp", "666666");
        if ("666666".equals(demoOtp)) {
            log.info("Using demo OTP: 666666");
            return "666666";
        }
        
        // Generate 6-digit OTP for production
        int otp = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
    
    @Override
    public String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() <= 3) {
            return "***";
        }
        
        if (isEmail(identifier)) {
            String[] parts = identifier.split("@");
            if (parts.length == 2) {
                String localPart = parts[0];
                String domain = parts[1];
                String maskedLocal = localPart.length() > 2 ? 
                    localPart.substring(0, 2) + "***" : "***";
                return maskedLocal + "@" + domain;
            }
        } else if (isPhone(identifier)) {
            int visibleDigits = 2;
            if (identifier.length() > visibleDigits + 3) {
                return identifier.substring(0, visibleDigits) + "***" + 
                       identifier.substring(identifier.length() - 2);
            }
        }
        
        return identifier.substring(0, 2) + "***";
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isExpired(OTPVerification otp) {
        return otp != null && otp.isExpired();
    }
    
    @Override
    public OTPVerification generateSignupOTP(String email, String sessionId) {
        return generateEmailOTP(email, OTPVerification.OTPType.EMAIL_VERIFICATION, null, sessionId);
    }
    
    @Override
    public OTPVerification generateMobileSignupOTP(String mobile, String sessionId) {
        return generatePhoneOTP(mobile, OTPVerification.OTPType.PHONE_VERIFICATION, null, sessionId);
    }
    
    @Override
    public OTPVerification generatePasswordResetOTP(String email) {
        return generateEmailOTP(email, OTPVerification.OTPType.PASSWORD_RESET, null, null);
    }
    
    @Override
    public OTPVerification generateOrderConfirmationOTP(String identifier, Long userId, String sessionId) {
        return generateOTP(identifier, OTPVerification.OTPType.ORDER_CONFIRMATION, userId, sessionId);
    }
    
    @Override
    public OTPVerification generateEmailChangeOTP(String newEmail, Long userId) {
        return generateEmailOTP(newEmail, OTPVerification.OTPType.EMAIL_CHANGE_VERIFICATION, userId, null);
    }
    
    // Helper methods
    private boolean isEmail(String identifier) {
        return identifier != null && EMAIL_PATTERN.matcher(identifier.trim()).matches();
    }
    
    private boolean isPhone(String identifier) {
        return identifier != null && PHONE_PATTERN.matcher(identifier.trim()).matches();
    }
}
