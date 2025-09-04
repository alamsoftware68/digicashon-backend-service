package com.repairapp.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating new orders with OTP verification
 */
public class CreateOrderWithOTPRequest {

    @NotEmpty(message = "Services list cannot be empty")
    @Valid
    private List<OrderServiceRequest> services;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @Size(max = 500, message = "Customer notes must not exceed 500 characters")
    private String customerNotes;

    @Size(max = 1000, message = "Problem description must not exceed 1000 characters")
    private String problemDescription;

    @NotBlank(message = "Contact identifier is required for OTP verification")
    private String contactIdentifier; // Email or phone for OTP

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{4,6}", message = "OTP must be 4-6 digits")
    private String otpCode;

    // Constructors
    public CreateOrderWithOTPRequest() {}

    public CreateOrderWithOTPRequest(List<OrderServiceRequest> services, BigDecimal totalAmount, 
                                   Long addressId, String contactIdentifier, String otpCode) {
        this.services = services;
        this.totalAmount = totalAmount;
        this.addressId = addressId;
        this.contactIdentifier = contactIdentifier;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public List<OrderServiceRequest> getServices() {
        return services;
    }

    public void setServices(List<OrderServiceRequest> services) {
        this.services = services;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getContactIdentifier() {
        return contactIdentifier;
    }

    public void setContactIdentifier(String contactIdentifier) {
        this.contactIdentifier = contactIdentifier;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    // Convert to basic CreateOrderRequest
    public CreateOrderRequest toBasicRequest() {
        CreateOrderRequest basicRequest = new CreateOrderRequest();
        basicRequest.setServices(this.services);
        basicRequest.setTotalAmount(this.totalAmount);
        basicRequest.setAddressId(this.addressId);
        basicRequest.setCustomerNotes(this.customerNotes);
        basicRequest.setProblemDescription(this.problemDescription);
        return basicRequest;
    }
}
