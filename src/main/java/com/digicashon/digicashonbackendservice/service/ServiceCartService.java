package com.digicashon.digicashonbackendservice.service;

import com.digicashon.digicashonbackendservice.dto.ServiceCartDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCartItemDto;
import com.digicashon.digicashonbackendservice.entity.ServiceCart;
import com.digicashon.digicashonbackendservice.entity.ServiceCartItem;
import com.repairapp.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing Service Cart operations
 */
public interface ServiceCartService {

    // Cart Management
    ServiceCart getOrCreateCart(String sessionId, User user);
    ServiceCart getActiveCart(String sessionId, User user);
    Optional<ServiceCart> findCartById(Long cartId);
    ServiceCartDto getCartDto(String sessionId, User user);
    void clearCart(String sessionId, User user);
    void deactivateCart(Long cartId);

    // Item Management
    ServiceCartItem addItemToCart(String sessionId, User user, Long serviceItemId, Integer quantity, String notes);
    ServiceCartItem updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity);
    void removeItemFromCart(String sessionId, User user, Long cartItemId);
    void removeServiceFromCart(String sessionId, User user, Long serviceItemId);
    List<ServiceCartItem> getCartItems(String sessionId, User user);
    List<ServiceCartItemDto> getCartItemsDto(String sessionId, User user);

    // Cart Operations
    ServiceCart refreshCartTotals(Long cartId);
    boolean isServiceInCart(String sessionId, User user, Long serviceItemId);
    int getCartItemCount(String sessionId, User user);
    double getCartTotal(String sessionId, User user);
    Map<String, Object> getCartSummary(String sessionId, User user);

    // Session Management
    ServiceCart transferSessionCartToUser(String sessionId, User user);
    ServiceCart mergeSessionCartWithUserCart(String sessionId, User user);
    void extendCartExpiry(Long cartId, int hours);

    // Validation
    boolean validateCartOwnership(Long cartId, String sessionId, User user);
    boolean isCartExpired(Long cartId);
    void validateCartItemQuantity(Integer quantity);
    void validateServiceForCart(Long serviceItemId);

    // Frontend Integration
    List<Map<String, Object>> getCartForFrontend(String sessionId, User user);
    Map<String, Object> addServiceForFrontend(String sessionId, User user, Long serviceItemId, Integer quantity, String notes);
    void removeServiceForFrontend(String sessionId, User user, Long serviceItemId);
    Map<String, Object> getCartSummaryForFrontend(String sessionId, User user);

    // Bulk Operations
    void addMultipleItems(String sessionId, User user, List<Map<String, Object>> items);
    void removeMultipleItems(String sessionId, User user, List<Long> serviceItemIds);
    void updateMultipleItems(String sessionId, User user, Map<Long, Integer> itemQuantities);

    // Cleanup Operations
    void cleanupExpiredCarts();
    void cleanupInactiveCarts(int daysOld);
    void cleanupEmptyCarts();

    // Analytics
    List<ServiceCart> getRecentCarts(int hours);
    List<ServiceCart> getAbandonedCarts(int hoursOld);
    long getActiveCartsCount();
    Map<String, Object> getCartAnalytics();

    // Utility Methods
    boolean isCartEmpty(Long cartId);
    void recalculateCartTotals(ServiceCart cart);
    ServiceCart saveCart(ServiceCart cart);
    ServiceCartItem saveCartItem(ServiceCartItem cartItem);
    
    // Service Item Integration
    boolean isServiceItemAvailable(Long serviceItemId);
    Double getServiceItemPrice(Long serviceItemId);
    String getServiceItemTitle(Long serviceItemId);
    String getServiceCategoryName(Long serviceItemId);
    
    // Order Placement
    Map<String, Object> placeOrderFromCart(String sessionId, User user, Long addressId, String problemDescription, String customerNotes);
}
