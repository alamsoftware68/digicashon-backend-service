package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Payment Method entity for user payment information
 */
@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentType type;

    @NotBlank(message = "Last 4 digits are required")
    @Pattern(regexp = "\\d{4}", message = "Last 4 digits must be 4 numeric characters")
    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Column(name = "brand", nullable = false, length = 50)
    private String brand; // Visa, MasterCard, American Express, etc.

    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be between 01 and 12")
    @Column(name = "expiry_month", length = 2)
    private String expiryMonth;

    @Pattern(regexp = "^\\d{4}$", message = "Expiry year must be 4 digits")
    @Column(name = "expiry_year", length = 4)
    private String expiryYear;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 100, message = "Cardholder name must not exceed 100 characters")
    @Column(name = "cardholder_name", length = 100)
    private String cardholderName;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Constructors
    public PaymentMethod() {}

    public PaymentMethod(PaymentType type, String lastFour, String brand, 
                        String expiryMonth, String expiryYear, Boolean isDefault) {
        this.type = type;
        this.lastFour = lastFour;
        this.brand = brand;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get masked card number for display
     */
    public String getMaskedCardNumber() {
        return "**** **** **** " + lastFour;
    }

    // Payment type enum
    public enum PaymentType {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        DIGITAL_WALLET("Digital Wallet"),
        BANK_TRANSFER("Bank Transfer");

        private final String displayName;

        PaymentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
