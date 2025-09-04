package com.repairapp.repository;

import com.repairapp.entity.OTPVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {
    
    // Find active OTP by identifier and type
    @Query("SELECT o FROM OTPVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    Optional<OTPVerification> findActiveOTPByIdentifierAndType(@Param("identifier") String identifier, 
                                                               @Param("otpType") OTPVerification.OTPType otpType,
                                                               @Param("currentTime") LocalDateTime currentTime);
    
    // Find latest OTP by identifier and type (regardless of status)
    Optional<OTPVerification> findTopByIdentifierAndOtpTypeOrderByCreatedAtDesc(String identifier, OTPVerification.OTPType otpType);
    
    // Find OTP by email and type
    @Query("SELECT o FROM OTPVerification o WHERE o.email = :email AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    Optional<OTPVerification> findActiveOTPByEmailAndType(@Param("email") String email, 
                                                          @Param("otpType") OTPVerification.OTPType otpType,
                                                          @Param("currentTime") LocalDateTime currentTime);
    
    // Find OTP by phone and type
    @Query("SELECT o FROM OTPVerification o WHERE o.phone = :phone AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    Optional<OTPVerification> findActiveOTPByPhoneAndType(@Param("phone") String phone, 
                                                          @Param("otpType") OTPVerification.OTPType otpType,
                                                          @Param("currentTime") LocalDateTime currentTime);
    
    // Find OTP by user ID and type
    @Query("SELECT o FROM OTPVerification o WHERE o.userId = :userId AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    Optional<OTPVerification> findActiveOTPByUserIdAndType(@Param("userId") Long userId, 
                                                           @Param("otpType") OTPVerification.OTPType otpType,
                                                           @Param("currentTime") LocalDateTime currentTime);
    
    // Find OTP by session ID and type
    @Query("SELECT o FROM OTPVerification o WHERE o.sessionId = :sessionId AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime ORDER BY o.createdAt DESC")
    Optional<OTPVerification> findActiveOTPBySessionIdAndType(@Param("sessionId") String sessionId, 
                                                              @Param("otpType") OTPVerification.OTPType otpType,
                                                              @Param("currentTime") LocalDateTime currentTime);
    
    // Find expired OTPs for cleanup
    @Query("SELECT o FROM OTPVerification o WHERE o.expiresAt < :currentTime")
    List<OTPVerification> findExpiredOTPs(@Param("currentTime") LocalDateTime currentTime);
    
    // Find OTPs by identifier (all types and statuses)
    List<OTPVerification> findByIdentifierOrderByCreatedAtDesc(String identifier);
    
    // Find verified OTPs by identifier and type
    @Query("SELECT o FROM OTPVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.isVerified = true ORDER BY o.verifiedAt DESC")
    List<OTPVerification> findVerifiedOTPsByIdentifierAndType(@Param("identifier") String identifier, @Param("otpType") OTPVerification.OTPType otpType);
    
    // Count active OTPs by identifier and type
    @Query("SELECT COUNT(o) FROM OTPVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.isVerified = false AND o.expiresAt > :currentTime")
    long countActiveOTPsByIdentifierAndType(@Param("identifier") String identifier, 
                                            @Param("otpType") OTPVerification.OTPType otpType,
                                            @Param("currentTime") LocalDateTime currentTime);
    
    // Count OTP attempts by identifier and type within time period
    @Query("SELECT COUNT(o) FROM OTPVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.createdAt >= :since")
    long countOTPAttemptsByIdentifierAndTypeWithinPeriod(@Param("identifier") String identifier, 
                                                         @Param("otpType") OTPVerification.OTPType otpType,
                                                         @Param("since") LocalDateTime since);
    
    // Find OTPs by type and status
    @Query("SELECT o FROM OTPVerification o WHERE o.otpType = :otpType AND o.isVerified = :isVerified ORDER BY o.createdAt DESC")
    List<OTPVerification> findByTypeAndStatus(@Param("otpType") OTPVerification.OTPType otpType, @Param("isVerified") Boolean isVerified);
    
    // Mark OTP as verified
    @Modifying
    @Query("UPDATE OTPVerification o SET o.isVerified = true, o.verifiedAt = :verifiedAt WHERE o.id = :otpId")
    int markAsVerified(@Param("otpId") Long otpId, @Param("verifiedAt") LocalDateTime verifiedAt);
    
    // Increment attempt count
    @Modifying
    @Query("UPDATE OTPVerification o SET o.attemptCount = o.attemptCount + 1 WHERE o.id = :otpId")
    int incrementAttemptCount(@Param("otpId") Long otpId);
    
    // Delete expired OTPs
    @Modifying
    @Query("DELETE FROM OTPVerification o WHERE o.expiresAt < :currentTime")
    int deleteExpiredOTPs(@Param("currentTime") LocalDateTime currentTime);
    
    // Delete old verified OTPs (cleanup)
    @Modifying
    @Query("DELETE FROM OTPVerification o WHERE o.isVerified = true AND o.verifiedAt < :cutoffTime")
    int deleteOldVerifiedOTPs(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Invalidate all active OTPs for identifier and type (when new OTP is generated)
    @Modifying
    @Query("UPDATE OTPVerification o SET o.isVerified = true, o.verifiedAt = :invalidatedAt WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.isVerified = false")
    int invalidateActiveOTPs(@Param("identifier") String identifier, 
                             @Param("otpType") OTPVerification.OTPType otpType,
                             @Param("invalidatedAt") LocalDateTime invalidatedAt);
    
    // Find recent OTPs by identifier (within specified minutes)
    @Query("SELECT o FROM OTPVerification o WHERE o.identifier = :identifier AND o.createdAt >= :since ORDER BY o.createdAt DESC")
    List<OTPVerification> findRecentOTPsByIdentifier(@Param("identifier") String identifier, @Param("since") LocalDateTime since);
    
    // Check if OTP exists and is valid
    @Query("SELECT COUNT(o) > 0 FROM OTPVerification o WHERE o.identifier = :identifier AND o.otpType = :otpType AND o.otpCode = :otpCode AND o.isVerified = false AND o.expiresAt > :currentTime AND o.attemptCount < o.maxAttempts")
    boolean existsValidOTP(@Param("identifier") String identifier, 
                           @Param("otpType") OTPVerification.OTPType otpType,
                           @Param("otpCode") String otpCode,
                           @Param("currentTime") LocalDateTime currentTime);
}
