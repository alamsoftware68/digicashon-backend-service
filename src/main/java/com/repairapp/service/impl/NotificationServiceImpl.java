package com.repairapp.service.impl;

import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
import com.repairapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.mail.from:noreply@repairapp.com}")
    private String fromEmail;
    
    @Value("${app.name:DigiCashOn Repair Service}")
    private String appName;
    
    @Override
    public void sendOTPEmail(OTPVerification otp) {
        if (otp.getEmail() == null) {
            log.warn("Cannot send OTP email - email is null for OTP ID: {}", otp.getId());
            return;
        }
        
        String subject = getOTPEmailSubject(otp.getOtpType());
        String content = buildOTPEmailContent(otp);
        
        sendEmail(otp.getEmail(), subject, content);
        log.info("OTP email sent to: {} for type: {}", otp.getEmail(), otp.getOtpType());
    }
    
    @Override
    public void sendOTPSMS(OTPVerification otp) {
        if (otp.getPhone() == null) {
            log.warn("Cannot send OTP SMS - phone is null for OTP ID: {}", otp.getId());
            return;
        }
        
        String message = buildOTPSMSContent(otp);
        sendSMS(otp.getPhone(), message);
        log.info("OTP SMS sent to: {} for type: {}", maskPhone(otp.getPhone()), otp.getOtpType());
    }
    
    @Override
    public void sendOTPNotification(OTPVerification otp) {
        if (otp.getEmail() != null) {
            sendOTPEmail(otp);
        }
        if (otp.getPhone() != null) {
            sendOTPSMS(otp);
        }
    }
    
    @Override
    public void sendOrderConfirmationEmail(RepairOrder order) {
        String subject = "Order Confirmation - " + order.getOrderNumber();
        String content = buildOrderConfirmationContent(order);
        
        sendEmail(order.getUser().getEmail(), subject, content);
        log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
    }
    
    @Override
    public void sendOrderStatusUpdateEmail(RepairOrder order, String previousStatus) {
        String subject = "Order Status Update - " + order.getOrderNumber();
        String content = buildOrderStatusUpdateContent(order, previousStatus);
        
        sendEmail(order.getUser().getEmail(), subject, content);
        log.info("Order status update email sent for order: {} - status: {}", 
            order.getOrderNumber(), order.getStatus());
    }
    
    @Override
    public void sendOrderCancellationEmail(RepairOrder order) {
        String subject = "Order Cancelled - " + order.getOrderNumber();
        String content = buildOrderCancellationContent(order);
        
        sendEmail(order.getUser().getEmail(), subject, content);
        log.info("Order cancellation email sent for order: {}", order.getOrderNumber());
    }
    
    @Override
    public void sendOrderCompletionEmail(RepairOrder order) {
        String subject = "Order Completed - " + order.getOrderNumber();
        String content = buildOrderCompletionContent(order);
        
        sendEmail(order.getUser().getEmail(), subject, content);
        log.info("Order completion email sent for order: {}", order.getOrderNumber());
    }
    
    @Override
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to " + appName;
        String content = buildWelcomeEmailContent(user);
        
        sendEmail(user.getEmail(), subject, content);
        log.info("Welcome email sent to: {}", user.getEmail());
    }
    
    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset Request";
        String content = buildPasswordResetContent(user, resetToken);
        
        sendEmail(user.getEmail(), subject, content);
        log.info("Password reset email sent to: {}", user.getEmail());
    }
    
    @Override
    public void sendPasswordChangeConfirmationEmail(User user) {
        String subject = "Password Changed Successfully";
        String content = buildPasswordChangeConfirmationContent(user);
        
        sendEmail(user.getEmail(), subject, content);
        log.info("Password change confirmation email sent to: {}", user.getEmail());
    }
    
    @Override
    public void sendEmailChangeConfirmationEmail(User user, String newEmail) {
        String subject = "Email Address Changed";
        String content = buildEmailChangeConfirmationContent(user, newEmail);
        
        sendEmail(newEmail, subject, content);
        log.info("Email change confirmation sent to new email: {}", newEmail);
    }
    
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.debug("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    @Override
    public void sendSMS(String phoneNumber, String message) {
        // For demo purposes, just log the SMS
        // In production, integrate with SMS service like Twilio, AWS SNS, etc.
        log.info("SMS would be sent to {}: {}", maskPhone(phoneNumber), message);
    }
    
    @Override
    public void sendTemplatedEmail(String to, String templateName, Object templateData) {
        // For demo purposes, use simple templates
        // In production, integrate with template engine like Thymeleaf
        log.info("Templated email would be sent to {} using template: {}", to, templateName);
    }
    
    @Override
    public void sendTemplatedSMS(String phoneNumber, String templateName, Object templateData) {
        log.info("Templated SMS would be sent to {} using template: {}", maskPhone(phoneNumber), templateName);
    }
    
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String content) {
        for (String recipient : recipients) {
            sendEmail(recipient, subject, content);
        }
        log.info("Bulk email sent to {} recipients", recipients.size());
    }
    
    @Override
    public void sendBulkSMS(List<String> phoneNumbers, String message) {
        for (String phone : phoneNumbers) {
            sendSMS(phone, message);
        }
        log.info("Bulk SMS sent to {} recipients", phoneNumbers.size());
    }
    
    @Override
    public boolean shouldSendEmailNotification(User user, NotificationType type) {
        // For demo purposes, always send email notifications
        // In production, check user preferences
        return user.getEmail() != null && !user.getEmail().isEmpty();
    }
    
    @Override
    public boolean shouldSendSMSNotification(User user, NotificationType type) {
        // For demo purposes, always send SMS if phone available
        // In production, check user preferences and SMS credits
        return user.getPhone() != null && !user.getPhone().isEmpty();
    }
    
    // Content builders
    private String getOTPEmailSubject(OTPVerification.OTPType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your Email Address";
            case PHONE_VERIFICATION -> "Verify Your Phone Number";
            case PASSWORD_RESET -> "Password Reset Verification";
            case ORDER_CONFIRMATION -> "Order Confirmation Code";
            case ACCOUNT_RECOVERY -> "Account Recovery Verification";
            case TWO_FACTOR_AUTH -> "Two-Factor Authentication Code";
            case EMAIL_CHANGE_VERIFICATION -> "Verify Your New Email Address";
        };
    }
    
    private String buildOTPEmailContent(OTPVerification otp) {
        String purpose = switch (otp.getOtpType()) {
            case EMAIL_VERIFICATION -> "verify your email address";
            case PHONE_VERIFICATION -> "verify your phone number";
            case PASSWORD_RESET -> "reset your password";
            case ORDER_CONFIRMATION -> "confirm your order";
            case ACCOUNT_RECOVERY -> "recover your account";
            case TWO_FACTOR_AUTH -> "complete two-factor authentication";
            case EMAIL_CHANGE_VERIFICATION -> "verify your new email address";
        };
        
        return String.format(
            "Dear Customer,\\n\\n" +
            "Your verification code to %s is: %s\\n\\n" +
            "This code will expire in 10 minutes.\\n" +
            "If you didn't request this code, please ignore this email.\\n\\n" +
            "Best regards,\\n%s Team",
            purpose, otp.getOtpCode(), appName
        );
    }
    
    private String buildOTPSMSContent(OTPVerification otp) {
        return String.format("Your %s verification code is: %s. Valid for 10 minutes. - %s", 
            appName, otp.getOtpCode(), appName);
    }
    
    private String buildOrderConfirmationContent(RepairOrder order) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your order has been confirmed!\\n\\n" +
            "Order Number: %s\\n" +
            "Total Amount: ₹%.2f\\n" +
            "Status: %s\\n\\n" +
            "We'll keep you updated on your order progress.\\n\\n" +
            "Best regards,\\n%s Team",
            order.getUser().getName(),
            order.getOrderNumber(),
            order.getTotalPrice().doubleValue(),
            order.getStatus(),
            appName
        );
    }
    
    private String buildOrderStatusUpdateContent(RepairOrder order, String previousStatus) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your order status has been updated:\\n\\n" +
            "Order Number: %s\\n" +
            "Previous Status: %s\\n" +
            "Current Status: %s\\n\\n" +
            "Thank you for choosing %s.\\n\\n" +
            "Best regards,\\n%s Team",
            order.getUser().getName(),
            order.getOrderNumber(),
            previousStatus,
            order.getStatus(),
            appName,
            appName
        );
    }
    
    private String buildOrderCancellationContent(RepairOrder order) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your order has been cancelled:\\n\\n" +
            "Order Number: %s\\n" +
            "Cancellation Reason: %s\\n\\n" +
            "If you have any questions, please contact our support team.\\n\\n" +
            "Best regards,\\n%s Team",
            order.getUser().getName(),
            order.getOrderNumber(),
            order.getTechnicianNotes() != null ? order.getTechnicianNotes() : "Cancelled by customer",
            appName
        );
    }
    
    private String buildOrderCompletionContent(RepairOrder order) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Great news! Your order has been completed:\\n\\n" +
            "Order Number: %s\\n" +
            "Completion Date: %s\\n\\n" +
            "Thank you for choosing %s for your repair needs!\\n\\n" +
            "Best regards,\\n%s Team",
            order.getUser().getName(),
            order.getOrderNumber(),
            order.getCompletedDate(),
            appName,
            appName
        );
    }
    
    private String buildWelcomeEmailContent(User user) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Welcome to %s!\\n\\n" +
            "Your account has been successfully created. You can now:\\n" +
            "- Book repair services for your devices\\n" +
            "- Track your orders in real-time\\n" +
            "- Manage your profile and addresses\\n\\n" +
            "Thank you for choosing us!\\n\\n" +
            "Best regards,\\n%s Team",
            user.getName(),
            appName,
            appName
        );
    }
    
    private String buildPasswordResetContent(User user, String resetToken) {
        return String.format(
            "Dear %s,\\n\\n" +
            "You requested a password reset for your %s account.\\n\\n" +
            "Reset Token: %s\\n\\n" +
            "If you didn't request this, please ignore this email.\\n\\n" +
            "Best regards,\\n%s Team",
            user.getName(),
            appName,
            resetToken,
            appName
        );
    }
    
    private String buildPasswordChangeConfirmationContent(User user) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your password has been successfully changed.\\n\\n" +
            "If you didn't make this change, please contact our support team immediately.\\n\\n" +
            "Best regards,\\n%s Team",
            user.getName(),
            appName
        );
    }
    
    private String buildEmailChangeConfirmationContent(User user, String newEmail) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your email address has been successfully changed to: %s\\n\\n" +
            "If you didn't make this change, please contact our support team immediately.\\n\\n" +
            "Best regards,\\n%s Team",
            user.getName(),
            newEmail,
            appName
        );
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return "****";
        }
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
