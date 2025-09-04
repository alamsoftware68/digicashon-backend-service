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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_session", columnList = "session_id"),
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_active", columnList = "is_active"),
    @Index(name = "idx_cart_expires", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", length = 255)
    private String sessionId; // For guest users
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_cart_user"))
    private User user; // For authenticated users
    
    @Column(name = "total_amount", nullable = false)
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
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // For optimistic locking
    
    // Utility methods
    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCart(this);
        updateTotals();
    }
    
    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
        updateTotals();
    }
    
    public void clearItems() {
        for (CartItem item : new ArrayList<>(cartItems)) {
            removeCartItem(item);
        }
    }
    
    public void updateTotals() {
        this.itemCount = cartItems.size();
        this.totalAmount = cartItems.stream()
            .mapToDouble(CartItem::getTotalPrice)
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
        return cartItems.isEmpty() || itemCount == 0;
    }
    
    // Set default expiry when cart is created
    @PrePersist
    protected void onCreate() {
        if (expiresAt == null) {
            // Default cart expiry: 24 hours for authenticated users, 2 hours for guests
            int defaultHours = (user != null) ? 24 : 2;
            expiresAt = LocalDateTime.now().plusHours(defaultHours);
        }
    }
}
