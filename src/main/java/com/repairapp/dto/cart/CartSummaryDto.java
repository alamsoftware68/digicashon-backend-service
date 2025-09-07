package com.repairapp.dto.cart;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart summary response
 */
public class CartSummaryDto {

    private Long cartId;
    private List<CartItemDto> items;
    private Integer itemCount;
    private Double totalAmount;
    private Boolean isEmpty;
    private LocalDateTime expiresAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CartSummaryDto() {}

    public CartSummaryDto(Long cartId, List<CartItemDto> items, Integer itemCount, 
                         Double totalAmount, Boolean isEmpty, LocalDateTime expiresAt, 
                         LocalDateTime updatedAt) {
        this.cartId = cartId;
        this.items = items;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
        this.isEmpty = isEmpty;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(Boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
