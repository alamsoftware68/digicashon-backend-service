package com.repairapp.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for placing order from cart
 */
public class PlaceOrderRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @Size(max = 1000, message = "Problem description must not exceed 1000 characters")
    private String problemDescription;

    @Size(max = 500, message = "Customer notes must not exceed 500 characters")
    private String customerNotes;

    private Boolean sendEmailNotification = true;
    private Boolean sendSmsNotification = true;

    // Constructors
    public PlaceOrderRequest() {}

    public PlaceOrderRequest(Long addressId, String problemDescription, String customerNotes) {
        this.addressId = addressId;
        this.problemDescription = problemDescription;
        this.customerNotes = customerNotes;
    }

    // Getters and Setters
    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public Boolean getSendEmailNotification() {
        return sendEmailNotification;
    }

    public void setSendEmailNotification(Boolean sendEmailNotification) {
        this.sendEmailNotification = sendEmailNotification;
    }

    public Boolean getSendSmsNotification() {
        return sendSmsNotification;
    }

    public void setSendSmsNotification(Boolean sendSmsNotification) {
        this.sendSmsNotification = sendSmsNotification;
    }
}
