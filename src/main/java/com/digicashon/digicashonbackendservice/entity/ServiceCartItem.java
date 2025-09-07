package com.digicashon.digicashonbackendservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ServiceCartItem entity for individual items in the service cart
 */
@Entity
@Table(name = "service_cart_items", indexes = {
    @Index(name = "idx_service_cart_item_cart", columnList = "service_cart_id"),
    @Index(name = "idx_service_cart_item_service", columnList = "service_item_id"),
    @Index(name = "idx_service_cart_item_cart_service", columnList = "service_cart_id, service_item_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_cart_item_cart"))
    @NotNull
    private ServiceCart serviceCart;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_cart_item_service"))
    @NotNull
    private ServiceItem serviceItem;
    
    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull
    private Integer quantity = 1;
    
    @Column(name = "unit_price", nullable = false, columnDefinition = "DECIMAL(12,2)")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    @NotNull
    private Double unitPrice;
    
    @Column(name = "total_price", nullable = false, columnDefinition = "DECIMAL(12,2)")
    @DecimalMin(value = "0.0", message = "Total price cannot be negative")
    @NotNull
    private Double totalPrice;
    
    @Column(name = "service_title", nullable = false, length = 200)
    @NotNull
    private String serviceTitle;
    
    @Column(name = "service_description", length = 1000)
    private String serviceDescription;
    
    @Column(name = "category_name", length = 100)
    private String categoryName;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // For optimistic locking
    
    // Constructor for easy creation
    public ServiceCartItem(ServiceCart serviceCart, ServiceItem serviceItem, Integer quantity, String notes) {
        this.serviceCart = serviceCart;
        this.serviceItem = serviceItem;
        this.quantity = quantity != null ? quantity : 1;
        this.notes = notes;
        
        // Copy service details for cart display
        if (serviceItem != null) {
            this.serviceTitle = serviceItem.getServiceTitle();
            this.serviceDescription = serviceItem.getServiceDescription();
            this.unitPrice = serviceItem.getPrice();
            
            if (serviceItem.getServiceCategory() != null) {
                this.categoryName = serviceItem.getServiceCategory().getCategoryName();
            }
        }
        
        calculateTotalPrice();
    }
    
    // Utility methods
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice * quantity;
        }
    }
    
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
        calculateTotalPrice();
    }
    
    public void updateUnitPrice(Double newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = newUnitPrice;
        calculateTotalPrice();
    }
    
    public boolean isSameService(ServiceItem service) {
        return this.serviceItem != null && service != null && 
               this.serviceItem.getId().equals(service.getId());
    }
    
    public boolean isSameService(Long serviceItemId) {
        return this.serviceItem != null && serviceItemId != null && 
               this.serviceItem.getId().equals(serviceItemId);
    }
    
    public void updateFromServiceItem() {
        if (this.serviceItem != null) {
            this.serviceTitle = serviceItem.getServiceTitle();
            this.serviceDescription = serviceItem.getServiceDescription();
            this.unitPrice = serviceItem.getPrice();
            
            if (serviceItem.getServiceCategory() != null) {
                this.categoryName = serviceItem.getServiceCategory().getCategoryName();
            }
            
            calculateTotalPrice();
        }
    }
    
    // Auto-calculate total price before save
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateTotalPrice();
    }
}
