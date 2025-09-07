package com.repairapp.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding a service to cart
 */
public class AddToCartRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Constructors
    public AddToCartRequest() {}

    public AddToCartRequest(Long serviceId, Integer quantity, String notes) {
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
