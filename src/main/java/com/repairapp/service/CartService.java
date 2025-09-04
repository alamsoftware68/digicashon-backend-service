package com.repairapp.service;

import com.repairapp.entity.Cart;
import com.repairapp.entity.CartItem;
import com.repairapp.entity.RepairService;
import com.repairapp.entity.User;

import java.util.List;
import java.util.Optional;

public interface CartService {
    
    // Cart Management
    Cart getOrCreateCart(String sessionId, User user);
    Cart getActiveCart(String sessionId, User user);
    Optional<Cart> findCartById(Long cartId);
    void clearCart(String sessionId, User user);
    void deactivateCart(Long cartId);
    
    // Item Management
    CartItem addItemToCart(String sessionId, User user, Long serviceId, Integer quantity, String notes);
    CartItem updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity);
    void removeItemFromCart(String sessionId, User user, Long cartItemId);
    List<CartItem> getCartItems(String sessionId, User user);
    
    // Cart Operations
    Cart refreshCartTotals(Long cartId);
    boolean isServiceInCart(String sessionId, User user, Long serviceId);
    int getCartItemCount(String sessionId, User user);
    double getCartTotal(String sessionId, User user);
    
    // Session Management
    Cart transferSessionCartToUser(String sessionId, User user);
    Cart mergeSessionCartWithUserCart(String sessionId, User user);
    void extendCartExpiry(Long cartId, int hours);
    
    // Validation
    boolean validateCartOwnership(Long cartId, String sessionId, User user);
    boolean isCartExpired(Long cartId);
    void validateCartItemQuantity(Integer quantity);
    void validateServiceForCart(RepairService service);
    
    // Cleanup Operations
    void cleanupExpiredCarts();
    void cleanupInactiveCarts(int daysOld);
    void cleanupEmptyCarts();
    
    // Analytics
    List<Cart> getRecentCarts(int hours);
    List<Cart> getAbandonedCarts(int hoursOld);
    long getActiveCartsCount();
    
    // Utility Methods
    boolean isCartEmpty(Long cartId);
    void recalculateCartTotals(Cart cart);
    Cart saveCart(Cart cart);
}
