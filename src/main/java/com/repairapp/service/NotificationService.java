package com.repairapp.service;

import com.repairapp.entity.DeviceEvaluation;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;

public interface NotificationService {

    // Order Placement Notifications (HTML + PDF)
    void sendSellOrderEmails(DeviceEvaluation evaluation, String customerName, String customerEmail, String customerPhone);
    void sendRepairOrderEmails(RepairOrder order, String customerName, String customerEmail, String customerPhone);

    // OTP Notifications
    void sendOTPEmail(OTPVerification otp);
    void sendOTPSMS(OTPVerification otp);
    void sendOTPNotification(OTPVerification otp);
    
    // Order Notifications
    void sendOrderConfirmationEmail(RepairOrder order);
    void sendOrderStatusUpdateEmail(RepairOrder order, String previousStatus);
    void sendOrderCancellationEmail(RepairOrder order);
    void sendOrderCompletionEmail(RepairOrder order);
    
    // User Account Notifications
    void sendWelcomeEmail(User user);
    void sendPasswordResetEmail(User user, String resetToken);
    void sendPasswordChangeConfirmationEmail(User user);
    void sendEmailChangeConfirmationEmail(User user, String newEmail);
    
    // Generic Email/SMS
    void sendEmail(String to, String subject, String content);
    void sendSMS(String phoneNumber, String message);
    
    // Template-based Notifications
    void sendTemplatedEmail(String to, String templateName, Object templateData);
    void sendTemplatedSMS(String phoneNumber, String templateName, Object templateData);
    
    // Bulk Notifications
    void sendBulkEmail(java.util.List<String> recipients, String subject, String content);
    void sendBulkSMS(java.util.List<String> phoneNumbers, String message);
    
    // Notification Preferences
    boolean shouldSendEmailNotification(User user, NotificationType type);
    boolean shouldSendSMSNotification(User user, NotificationType type);
    
    enum NotificationType {
        OTP_VERIFICATION,
        ORDER_CONFIRMATION,
        ORDER_STATUS_UPDATE,
        ORDER_CANCELLATION,
        ORDER_COMPLETION,
        WELCOME,
        PASSWORD_RESET,
        PASSWORD_CHANGE,
        EMAIL_CHANGE,
        PROMOTIONAL,
        REMINDER
    }
}
