package com.repairapp.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating new orders with multiple services
 */
public class CreateOrderRequest {

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

    // Constructors
    public CreateOrderRequest() {}

    public CreateOrderRequest(List<OrderServiceRequest> services, BigDecimal totalAmount, Long addressId) {
        this.services = services;
        this.totalAmount = totalAmount;
        this.addressId = addressId;
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
}
