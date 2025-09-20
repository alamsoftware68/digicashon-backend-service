package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Order Item entity representing individual services within an order
 */
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Column(name = "unit_price", nullable = false, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal unitPrice;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(name = "total_price", nullable = false, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalPrice;

    @Column(name = "notes", length = 500)
    private String notes;

    // *** ENHANCED FIELDS TO STORE PROPER CATEGORY AND SERVICE ITEM DETAILS ***
    @Column(name = "service_name")
    @JsonProperty("serviceName") // Ensure this field is included in JSON response
    private String serviceName; // "MacBook Pro Screen Repair" - from request

    @Column(name = "service_category") 
    @JsonProperty("serviceCategory") // Ensure this field is included in JSON response
    private String serviceCategory; // "Home Visit" - from service_categories table

    @Column(name = "service_title")
    @JsonProperty("serviceTitle") // Ensure this field is included in JSON response
    private String serviceTitle; // "MacBook" - from service_items table

    // NEW: Reference to actual service item from service_items table
    @Column(name = "service_item_id")
    @JsonProperty("serviceItemId") // Ensure this field is included in JSON response
    private Long serviceItemId; // ID from service_items table

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private RepairOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnoreProperties({"orders", "orderItems", "features", "category"}) // Prevent circular references and lazy loading issues
    private RepairService service;

    // Constructors
    public OrderItem() {}

    public OrderItem(RepairOrder order, RepairService service, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.service = service;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // NEW Constructor with cart item details
    public OrderItem(RepairOrder order, RepairService service, Integer quantity, BigDecimal unitPrice, 
                     String serviceName, String serviceCategory) {
        this.order = order;
        this.service = service;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.serviceName = serviceName;
        this.serviceCategory = serviceCategory;
    }

    // Getters and Setters
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculate total price when quantity changes
        if (this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        // Recalculate total price when unit price changes
        if (this.quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public RepairOrder getOrder() {
        return order;
    }

    public void setOrder(RepairOrder order) {
        this.order = order;
    }

    public RepairService getService() {
        return service;
    }

    public void setService(RepairService service) {
        this.service = service;
    }

    // *** ENHANCED GETTERS AND SETTERS FOR SERVICE DETAILS ***
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }

    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }

    public Long getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
}
