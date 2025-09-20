package com.repairapp.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for individual service within an order
 */
public class OrderServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Service price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Service price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Quantity is required")
    private Integer quantity = 1;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // *** NEW FIELDS FOR CATEGORY INFORMATION ***
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName; // "Home Visit", "Repair", "Laptop/Desktop", "Add-On"

    @Size(max = 150, message = "Service title must not exceed 150 characters")
    private String serviceTitle; // "Windows Laptop", "Desktop", "MacBook"

    // Constructors
    public OrderServiceRequest() {}

    public OrderServiceRequest(String name, BigDecimal price, Long serviceId) {
        this.name = name;
        this.price = price;
        this.serviceId = serviceId;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

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

    // *** NEW GETTERS AND SETTERS FOR CATEGORY INFORMATION ***
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }
}
