package com.repairapp.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order confirmation response
 */
public class OrderConfirmationDto {

    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private Integer totalServices;
    private String status;
    private String pickupAddress;
    private LocalDateTime createdAt;
    private String message;
    private Boolean emailSent;
    private Boolean smsSent;

    // Constructors
    public OrderConfirmationDto() {}

    public OrderConfirmationDto(Long orderId, String orderNumber, BigDecimal totalAmount, 
                               Integer totalServices, String status, String pickupAddress,
                               LocalDateTime createdAt, String message, 
                               Boolean emailSent, Boolean smsSent) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.totalServices = totalServices;
        this.status = status;
        this.pickupAddress = pickupAddress;
        this.createdAt = createdAt;
        this.message = message;
        this.emailSent = emailSent;
        this.smsSent = smsSent;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalServices() {
        return totalServices;
    }

    public void setTotalServices(Integer totalServices) {
        this.totalServices = totalServices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public Boolean getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Boolean smsSent) {
        this.smsSent = smsSent;
    }
}
