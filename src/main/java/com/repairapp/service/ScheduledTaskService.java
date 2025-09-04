package com.repairapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {
    
    private final CartService cartService;
    private final OTPService otpService;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredCarts() {
        try {
            log.info("Starting scheduled cleanup of expired carts");
            cartService.cleanupExpiredCarts();
            log.info("Completed scheduled cleanup of expired carts");
        } catch (Exception e) {
            log.error("Error during scheduled cart cleanup", e);
        }
    }
    
    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    public void cleanupExpiredOTPs() {
        try {
            log.info("Starting scheduled cleanup of expired OTPs");
            otpService.cleanupExpiredOTPs();
            log.info("Completed scheduled cleanup of expired OTPs");
        } catch (Exception e) {
            log.error("Error during scheduled OTP cleanup", e);
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    public void cleanupInactiveCarts() {
        try {
            log.info("Starting scheduled cleanup of inactive carts");
            cartService.cleanupInactiveCarts(7); // Remove carts inactive for 7+ days
            log.info("Completed scheduled cleanup of inactive carts");
        } catch (Exception e) {
            log.error("Error during scheduled inactive cart cleanup", e);
        }
    }
    
    @Scheduled(cron = "0 30 2 * * ?") // Every day at 2:30 AM
    public void cleanupVerifiedOTPs() {
        try {
            log.info("Starting scheduled cleanup of old verified OTPs");
            otpService.cleanupVerifiedOTPs(30); // Remove verified OTPs older than 30 days
            log.info("Completed scheduled cleanup of old verified OTPs");
        } catch (Exception e) {
            log.error("Error during scheduled verified OTP cleanup", e);
        }
    }
    
    @Scheduled(fixedRate = 7200000) // Every 2 hours
    public void cleanupEmptyCarts() {
        try {
            log.info("Starting scheduled cleanup of empty carts");
            cartService.cleanupEmptyCarts();
            log.info("Completed scheduled cleanup of empty carts");
        } catch (Exception e) {
            log.error("Error during scheduled empty cart cleanup", e);
        }
    }
}
