package com.repairapp.entity;

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

@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_service", columnList = "repair_service_id"),
    @Index(name = "idx_cart_item_cart_service", columnList = "cart_id, repair_service_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    @NotNull
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_service_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_service"))
    @NotNull
    private RepairService repairService;
    
    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false)
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    @NotNull
    private Double unitPrice;
    
    @Column(name = "total_price", nullable = false)
    @DecimalMin(value = "0.0", message = "Total price cannot be negative")
    @NotNull
    private Double totalPrice;
    
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
    
    public boolean isSameService(RepairService service) {
        return this.repairService != null && service != null && 
               this.repairService.getId().equals(service.getId());
    }
    
    // Auto-calculate total price before save
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateTotalPrice();
    }
}
