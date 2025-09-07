package com.digicashon.digicashonbackendservice.entity;

import com.repairapp.entity.User;
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
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceCart entity for managing cart items with Service Categories services
 */
@Entity
@Table(name = "service_carts", indexes = {
    @Index(name = "idx_service_cart_session", columnList = "session_id"),
    @Index(name = "idx_service_cart_user", columnList = "user_id"),
    @Index(name = "idx_service_cart_active", columnList = "is_active"),
    @Index(name = "idx_service_cart_expires", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", length = 255)
    private String sessionId; // For guest users
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_service_cart_user"))
    private User user; // For authenticated users
    
    @Column(name = "total_amount", nullable = false, columnDefinition = "DECIMAL(12,2)")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @NotNull
    private Double totalAmount = 0.0;
    
    @Column(name = "item_count", nullable = false)
    @Min(value = 0, message = "Item count cannot be negative")
    @NotNull
    private Integer itemCount = 0;
    
    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;
    
    @Column(name = "expires_at", nullable = false)
    @NotNull
    private LocalDateTime expiresAt;
    
    @OneToMany(mappedBy = "serviceCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ServiceCartItem> serviceCartItems = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // For optimistic locking
    
    // Utility methods
    public void addServiceCartItem(ServiceCartItem serviceCartItem) {
        serviceCartItems.add(serviceCartItem);
        serviceCartItem.setServiceCart(this);
        updateTotals();
    }
    
    public void removeServiceCartItem(ServiceCartItem serviceCartItem) {
        serviceCartItems.remove(serviceCartItem);
        serviceCartItem.setServiceCart(null);
        updateTotals();
    }
    
    public void clearItems() {
        for (ServiceCartItem item : new ArrayList<>(serviceCartItems)) {
            removeServiceCartItem(item);
        }
    }
    
    public void updateTotals() {
        this.itemCount = serviceCartItems.size();
        this.totalAmount = serviceCartItems.stream()
            .mapToDouble(ServiceCartItem::getTotalPrice)
            .sum();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isOwnedBy(String sessionId, User user) {
        if (user != null && this.user != null) {
            return this.user.getId().equals(user.getId());
        }
        return sessionId != null && sessionId.equals(this.sessionId);
    }
    
    public void extendExpiry(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }
    
    public boolean isEmpty() {
        return serviceCartItems.isEmpty() || itemCount == 0;
    }
    
    public boolean hasServiceItem(Long serviceItemId) {
        return serviceCartItems.stream()
            .anyMatch(item -> item.getServiceItem().getId().equals(serviceItemId));
    }
    
    // Set default expiry when cart is created
    @PrePersist
    protected void onCreate() {
        if (expiresAt == null) {
            // Default cart expiry: 24 hours for authenticated users, 4 hours for guests
            int defaultHours = (user != null) ? 24 : 4;
            expiresAt = LocalDateTime.now().plusHours(defaultHours);
        }
    }
}
