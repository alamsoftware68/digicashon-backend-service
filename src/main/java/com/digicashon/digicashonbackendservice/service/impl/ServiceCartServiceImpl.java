package com.digicashon.digicashonbackendservice.service.impl;

import com.digicashon.digicashonbackendservice.dto.ServiceCartDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCartItemDto;
import com.digicashon.digicashonbackendservice.entity.ServiceCart;
import com.digicashon.digicashonbackendservice.entity.ServiceCartItem;
import com.digicashon.digicashonbackendservice.entity.ServiceItem;
import com.digicashon.digicashonbackendservice.repository.ServiceCartItemRepository;
import com.digicashon.digicashonbackendservice.repository.ServiceCartRepository;
import com.digicashon.digicashonbackendservice.repository.ServiceItemRepository;
import com.digicashon.digicashonbackendservice.service.ServiceCartService;
import com.repairapp.dto.order.OrderServiceRequest;
import com.repairapp.entity.*;
import com.repairapp.repository.AddressRepository;
import com.repairapp.service.OrderService;
import com.repairapp.util.OrderNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ServiceCartService for managing Service Cart operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceCartServiceImpl implements ServiceCartService {

    private final ServiceCartRepository serviceCartRepository;
    private final ServiceCartItemRepository serviceCartItemRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AddressRepository addressRepository;
    private final OrderService orderService;

    @Override
    public ServiceCart getOrCreateCart(String sessionId, User user) {
        log.debug("Getting or creating cart for sessionId: {}, user: {}", sessionId, user != null ? user.getId() : null);

        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart == null) {
            cart = new ServiceCart();
            cart.setSessionId(sessionId);
            cart.setUser(user);
            cart.setIsActive(true);
            cart.setTotalAmount(0.0);
            cart.setItemCount(0);
            
            cart = serviceCartRepository.save(cart);
            log.info("Created new cart with ID: {} for sessionId: {}, user: {}", 
                    cart.getId(), sessionId, user != null ? user.getId() : null);
        }

        return cart;
    }

    @Override
    public ServiceCart getActiveCart(String sessionId, User user) {
        if (user != null) {
            // For authenticated users, first try to find by user ID
            Optional<ServiceCart> userCart = serviceCartRepository.findByUserIdAndIsActiveTrue(user.getId());
            if (userCart.isPresent()) {
                return userCart.get();
            }
            
            // If no user cart exists, check for session cart and merge it
            if (sessionId != null) {
                Optional<ServiceCart> sessionCart = serviceCartRepository.findBySessionIdAndIsActiveTrue(sessionId);
                if (sessionCart.isPresent()) {
                    // Transfer session cart to user
                    ServiceCart cart = sessionCart.get();
                    cart.setUser(user);
                    cart.setSessionId(null); // Clear session ID since it's now a user cart
                    return serviceCartRepository.save(cart);
                }
            }
            return null;
        } else if (sessionId != null) {
            // For guest users, use session cart
            return serviceCartRepository.findBySessionIdAndIsActiveTrue(sessionId).orElse(null);
        }
        return null;
    }

    @Override
    public Optional<ServiceCart> findCartById(Long cartId) {
        return serviceCartRepository.findById(cartId);
    }

    @Override
    public ServiceCartDto getCartDto(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        return ServiceCartDto.fromEntity(cart);
    }

    @Override
    public void clearCart(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart != null) {
            cart.clearItems();
            serviceCartRepository.save(cart);
            log.info("Cleared cart ID: {} for sessionId: {}, user: {}", 
                    cart.getId(), sessionId, user != null ? user.getId() : null);
        }
    }

    @Override
    public void deactivateCart(Long cartId) {
        serviceCartRepository.deactivateCart(cartId);
        log.info("Deactivated cart ID: {}", cartId);
    }

    @Override
    public ServiceCartItem addItemToCart(String sessionId, User user, Long serviceItemId, Integer quantity, String notes) {
        log.info("Adding item to cart - serviceItemId: {}, quantity: {}, sessionId: {}, user: {}", 
                serviceItemId, quantity, sessionId, user != null ? user.getId() : null);

        // Validate inputs
        validateServiceForCart(serviceItemId);
        validateCartItemQuantity(quantity);

        ServiceCart cart = getOrCreateCart(sessionId, user);
        ServiceItem serviceItem = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new RuntimeException("Service item not found with ID: " + serviceItemId));

        // Check if item already exists in cart
        Optional<ServiceCartItem> existingItem = serviceCartItemRepository
                .findByCartIdAndServiceItemId(cart.getId(), serviceItemId);

        ServiceCartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item quantity
            cartItem = existingItem.get();
            cartItem.updateQuantity(cartItem.getQuantity() + (quantity != null ? quantity : 1));
            if (notes != null && !notes.trim().isEmpty()) {
                cartItem.setNotes(notes);
            }
        } else {
            // Create new cart item
            cartItem = new ServiceCartItem(cart, serviceItem, quantity, notes);
            cart.addServiceCartItem(cartItem);
        }

        cartItem = serviceCartItemRepository.save(cartItem);
        cart.updateTotals();
        serviceCartRepository.save(cart);

        log.info("Added item to cart - cartItemId: {}, cartId: {}, serviceItemId: {}, quantity: {}", 
                cartItem.getId(), cart.getId(), serviceItemId, cartItem.getQuantity());

        return cartItem;
    }

    @Override
    public ServiceCartItem updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity) {
        log.info("Updating cart item quantity - cartItemId: {}, quantity: {}", cartItemId, quantity);

        validateCartItemQuantity(quantity);

        ServiceCartItem cartItem = serviceCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with ID: " + cartItemId));

        ServiceCart cart = cartItem.getServiceCart();
        if (!validateCartOwnership(cart.getId(), sessionId, user)) {
            throw new RuntimeException("You don't have permission to modify this cart item");
        }

        cartItem.updateQuantity(quantity);
        cartItem = serviceCartItemRepository.save(cartItem);

        cart.updateTotals();
        serviceCartRepository.save(cart);

        log.info("Updated cart item quantity - cartItemId: {}, newQuantity: {}", cartItemId, quantity);
        return cartItem;
    }

    @Override
    public void removeItemFromCart(String sessionId, User user, Long cartItemId) {
        log.info("Removing cart item - cartItemId: {}", cartItemId);

        ServiceCartItem cartItem = serviceCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with ID: " + cartItemId));

        ServiceCart cart = cartItem.getServiceCart();
        if (!validateCartOwnership(cart.getId(), sessionId, user)) {
            throw new RuntimeException("You don't have permission to modify this cart item");
        }

        cart.removeServiceCartItem(cartItem);
        serviceCartItemRepository.delete(cartItem);

        cart.updateTotals();
        serviceCartRepository.save(cart);

        log.info("Removed cart item - cartItemId: {}, cartId: {}", cartItemId, cart.getId());
    }

    @Override
    public void removeServiceFromCart(String sessionId, User user, Long serviceItemId) {
        log.info("Removing service from cart - serviceItemId: {}", serviceItemId);

        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart == null) {
            log.warn("No active cart found for sessionId: {}, user: {}", sessionId, user != null ? user.getId() : null);
            return;
        }

        Optional<ServiceCartItem> cartItem = serviceCartItemRepository
                .findByCartIdAndServiceItemId(cart.getId(), serviceItemId);

        if (cartItem.isPresent()) {
            cart.removeServiceCartItem(cartItem.get());
            serviceCartItemRepository.delete(cartItem.get());

            cart.updateTotals();
            serviceCartRepository.save(cart);

            log.info("Removed service from cart - serviceItemId: {}, cartId: {}", serviceItemId, cart.getId());
        } else {
            log.warn("Service not found in cart - serviceItemId: {}, cartId: {}", serviceItemId, cart.getId());
        }
    }

    @Override
    public List<ServiceCartItem> getCartItems(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart != null) {
            return serviceCartItemRepository.findByCartIdWithFullDetails(cart.getId());
        }
        return new ArrayList<>();
    }

    @Override
    public List<ServiceCartItemDto> getCartItemsDto(String sessionId, User user) {
        return getCartItems(sessionId, user).stream()
                .map(ServiceCartItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceCart refreshCartTotals(Long cartId) {
        ServiceCart cart = serviceCartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));

        cart.updateTotals();
        return serviceCartRepository.save(cart);
    }

    @Override
    public boolean isServiceInCart(String sessionId, User user, Long serviceItemId) {
        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart != null) {
            return serviceCartItemRepository.existsByCartIdAndServiceItemId(cart.getId(), serviceItemId);
        }
        return false;
    }

    @Override
    public int getCartItemCount(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        return cart != null ? cart.getItemCount() : 0;
    }

    @Override
    public double getCartTotal(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        return cart != null ? cart.getTotalAmount() : 0.0;
    }

    @Override
    public Map<String, Object> getCartSummary(String sessionId, User user) {
        ServiceCart cart = getActiveCart(sessionId, user);
        Map<String, Object> summary = new HashMap<>();

        if (cart != null) {
            List<ServiceCartItem> items = serviceCartItemRepository.findByCartIdWithFullDetails(cart.getId());
            
            summary.put("cartId", cart.getId());
            summary.put("itemCount", cart.getItemCount());
            summary.put("totalAmount", cart.getTotalAmount());
            summary.put("isEmpty", cart.isEmpty());
            summary.put("isExpired", cart.isExpired());
            summary.put("expiresAt", cart.getExpiresAt());
            
            // Calculate additional metrics
            int totalQuantity = items.stream().mapToInt(ServiceCartItem::getQuantity).sum();
            Set<String> categories = items.stream()
                    .map(ServiceCartItem::getCategoryName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            summary.put("totalQuantity", totalQuantity);
            summary.put("categoriesCount", categories.size());
            summary.put("categories", new ArrayList<>(categories));
        } else {
            summary.put("cartId", null);
            summary.put("itemCount", 0);
            summary.put("totalAmount", 0.0);
            summary.put("isEmpty", true);
            summary.put("isExpired", false);
            summary.put("totalQuantity", 0);
            summary.put("categoriesCount", 0);
            summary.put("categories", new ArrayList<>());
        }

        return summary;
    }

    @Override
    public ServiceCart transferSessionCartToUser(String sessionId, User user) {
        if (sessionId == null || user == null) {
            return null;
        }

        log.info("Transferring session cart to user - sessionId: {}, userId: {}", sessionId, user.getId());

        // Find session cart
        Optional<ServiceCart> sessionCart = serviceCartRepository.findBySessionIdAndIsActiveTrue(sessionId);
        if (sessionCart.isPresent()) {
            ServiceCart cart = sessionCart.get();
            cart.setUser(user);
            cart.setSessionId(null); // Clear session ID
            cart.extendExpiry(24); // Extend expiry for authenticated user

            cart = serviceCartRepository.save(cart);
            log.info("Transferred session cart to user - cartId: {}, userId: {}", cart.getId(), user.getId());
            return cart;
        }

        return null;
    }

    @Override
    public ServiceCart mergeSessionCartWithUserCart(String sessionId, User user) {
        if (sessionId == null || user == null) {
            return null;
        }

        log.info("Merging session cart with user cart - sessionId: {}, userId: {}", sessionId, user.getId());

        Optional<ServiceCart> sessionCart = serviceCartRepository.findBySessionIdAndIsActiveTrue(sessionId);
        Optional<ServiceCart> userCart = serviceCartRepository.findByUserIdAndIsActiveTrue(user.getId());

        if (sessionCart.isPresent() && userCart.isPresent()) {
            // Merge session cart items into user cart
            ServiceCart source = sessionCart.get();
            ServiceCart target = userCart.get();

            List<ServiceCartItem> sessionItems = serviceCartItemRepository.findByCartId(source.getId());
            for (ServiceCartItem sessionItem : sessionItems) {
                Optional<ServiceCartItem> existingItem = serviceCartItemRepository
                        .findByCartIdAndServiceItemId(target.getId(), sessionItem.getServiceItem().getId());

                if (existingItem.isPresent()) {
                    // Update existing item quantity
                    ServiceCartItem existing = existingItem.get();
                    existing.updateQuantity(existing.getQuantity() + sessionItem.getQuantity());
                    serviceCartItemRepository.save(existing);
                } else {
                    // Move item to user cart
                    sessionItem.setServiceCart(target);
                    target.addServiceCartItem(sessionItem);
                    serviceCartItemRepository.save(sessionItem);
                }
            }

            // Delete session cart
            serviceCartRepository.delete(source);

            // Update user cart totals
            target.updateTotals();
            target = serviceCartRepository.save(target);

            log.info("Merged session cart with user cart - sessionCartId: {}, userCartId: {}, userId: {}", 
                    source.getId(), target.getId(), user.getId());
            return target;

        } else if (sessionCart.isPresent() && userCart.isEmpty()) {
            // Transfer session cart to user
            return transferSessionCartToUser(sessionId, user);
        } else if (userCart.isPresent()) {
            // Return existing user cart
            return userCart.get();
        }

        return null;
    }

    @Override
    public void extendCartExpiry(Long cartId, int hours) {
        ServiceCart cart = serviceCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));

        cart.extendExpiry(hours);
        serviceCartRepository.save(cart);

        log.info("Extended cart expiry - cartId: {}, hours: {}", cartId, hours);
    }

    @Override
    public boolean validateCartOwnership(Long cartId, String sessionId, User user) {
        if (user != null) {
            return serviceCartRepository.existsByIdAndOwnership(cartId, sessionId, user.getId());
        } else if (sessionId != null) {
            return serviceCartRepository.existsByIdAndOwnership(cartId, sessionId, null);
        }
        return false;
    }

    @Override
    public boolean isCartExpired(Long cartId) {
        ServiceCart cart = serviceCartRepository.findById(cartId).orElse(null);
        return cart != null && cart.isExpired();
    }

    @Override
    public void validateCartItemQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (quantity > 100) { // Reasonable upper limit
            throw new IllegalArgumentException("Quantity cannot exceed 100");
        }
    }

    @Override
    public void validateServiceForCart(Long serviceItemId) {
        if (serviceItemId == null) {
            throw new IllegalArgumentException("Service item ID cannot be null");
        }
        if (!isServiceItemAvailable(serviceItemId)) {
            throw new RuntimeException("Service item not found or not available with ID: " + serviceItemId);
        }
    }

    // Frontend Integration Methods
    @Override
    public List<Map<String, Object>> getCartForFrontend(String sessionId, User user) {
        List<ServiceCartItem> items = getCartItems(sessionId, user);
        return items.stream().map(item -> {
            Map<String, Object> frontendItem = new HashMap<>();
            frontendItem.put("id", item.getId());
            frontendItem.put("name", item.getServiceTitle());
            frontendItem.put("price", item.getUnitPrice());
            frontendItem.put("quantity", item.getQuantity());
            frontendItem.put("totalPrice", item.getTotalPrice());
            frontendItem.put("category", item.getCategoryName());
            frontendItem.put("description", item.getServiceDescription());
            frontendItem.put("serviceItemId", item.getServiceItem().getId());
            frontendItem.put("notes", item.getNotes());
            frontendItem.put("addedAt", item.getCreatedAt());
            return frontendItem;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> addServiceForFrontend(String sessionId, User user, Long serviceItemId, Integer quantity, String notes) {
        ServiceCartItem cartItem = addItemToCart(sessionId, user, serviceItemId, quantity, notes);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Service added to cart successfully");
        result.put("cartItem", Map.of(
                "id", cartItem.getId(),
                "name", cartItem.getServiceTitle(),
                "price", cartItem.getUnitPrice(),
                "quantity", cartItem.getQuantity(),
                "totalPrice", cartItem.getTotalPrice()
        ));
        result.put("cartSummary", getCartSummaryForFrontend(sessionId, user));
        return result;
    }

    @Override
    public void removeServiceForFrontend(String sessionId, User user, Long serviceItemId) {
        removeServiceFromCart(sessionId, user, serviceItemId);
    }

    @Override
    public Map<String, Object> getCartSummaryForFrontend(String sessionId, User user) {
        return getCartSummary(sessionId, user);
    }

    // Bulk Operations
    @Override
    public void addMultipleItems(String sessionId, User user, List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long serviceItemId = Long.valueOf(item.get("serviceItemId").toString());
            Integer quantity = Integer.valueOf(item.getOrDefault("quantity", 1).toString());
            String notes = (String) item.get("notes");
            addItemToCart(sessionId, user, serviceItemId, quantity, notes);
        }
    }

    @Override
    public void removeMultipleItems(String sessionId, User user, List<Long> serviceItemIds) {
        for (Long serviceItemId : serviceItemIds) {
            removeServiceFromCart(sessionId, user, serviceItemId);
        }
    }

    @Override
    public void updateMultipleItems(String sessionId, User user, Map<Long, Integer> itemQuantities) {
        ServiceCart cart = getActiveCart(sessionId, user);
        if (cart != null) {
            for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
                Optional<ServiceCartItem> cartItem = serviceCartItemRepository
                        .findByCartIdAndServiceItemId(cart.getId(), entry.getKey());
                if (cartItem.isPresent()) {
                    updateCartItemQuantity(sessionId, user, cartItem.get().getId(), entry.getValue());
                }
            }
        }
    }

    // Cleanup Operations
    @Override
    public void cleanupExpiredCarts() {
        List<ServiceCart> expiredCarts = serviceCartRepository.findExpiredCarts(LocalDateTime.now());
        for (ServiceCart cart : expiredCarts) {
            cart.setIsActive(false);
            serviceCartRepository.save(cart);
        }
        log.info("Cleaned up {} expired carts", expiredCarts.size());
    }

    @Override
    public void cleanupInactiveCarts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<ServiceCart> oldCarts = serviceCartRepository.findCartsOlderThan(cutoffDate);
        serviceCartRepository.deleteAll(oldCarts);
        log.info("Cleaned up {} inactive carts older than {} days", oldCarts.size(), daysOld);
    }

    @Override
    public void cleanupEmptyCarts() {
        List<ServiceCart> emptyCarts = serviceCartRepository.findEmptyCarts();
        for (ServiceCart cart : emptyCarts) {
            cart.setIsActive(false);
            serviceCartRepository.save(cart);
        }
        log.info("Cleaned up {} empty carts", emptyCarts.size());
    }

    // Analytics
    @Override
    public List<ServiceCart> getRecentCarts(int hours) {
        LocalDateTime sinceTime = LocalDateTime.now().minusHours(hours);
        return serviceCartRepository.findRecentCarts(sinceTime);
    }

    @Override
    public List<ServiceCart> getAbandonedCarts(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        return serviceCartRepository.findAbandonedCarts(cutoffTime);
    }

    @Override
    public long getActiveCartsCount() {
        return serviceCartRepository.countActiveCarts();
    }

    @Override
    public Map<String, Object> getCartAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("activeCartsCount", getActiveCartsCount());
        analytics.put("recentCartsCount", getRecentCarts(24).size());
        analytics.put("abandonedCartsCount", getAbandonedCarts(72).size());
        return analytics;
    }

    // Utility Methods
    @Override
    public boolean isCartEmpty(Long cartId) {
        ServiceCart cart = serviceCartRepository.findById(cartId).orElse(null);
        return cart == null || cart.isEmpty();
    }

    @Override
    public void recalculateCartTotals(ServiceCart cart) {
        cart.updateTotals();
        serviceCartRepository.save(cart);
    }

    @Override
    public ServiceCart saveCart(ServiceCart cart) {
        return serviceCartRepository.save(cart);
    }

    @Override
    public ServiceCartItem saveCartItem(ServiceCartItem cartItem) {
        return serviceCartItemRepository.save(cartItem);
    }

    // Service Item Integration
    @Override
    public boolean isServiceItemAvailable(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .map(ServiceItem::getIsActive)
                .orElse(false);
    }

    @Override
    public Double getServiceItemPrice(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .map(ServiceItem::getPrice)
                .orElse(null);
    }

    @Override
    public String getServiceItemTitle(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .map(ServiceItem::getServiceTitle)
                .orElse(null);
    }

    @Override
    public String getServiceCategoryName(Long serviceItemId) {
        return serviceItemRepository.findById(serviceItemId)
                .map(serviceItem -> serviceItem.getServiceCategory() != null ? 
                     serviceItem.getServiceCategory().getCategoryName() : null)
                .orElse(null);
    }

    // Order Placement Implementation
    @Override
    public Map<String, Object> placeOrderFromCart(String sessionId, User user, Long addressId, String problemDescription, String customerNotes) {
        if (user == null) {
            throw new RuntimeException("User must be authenticated to place order");
        }

        log.info("Placing order from cart for user: {}, sessionId: {}, addressId: {}", user.getId(), sessionId, addressId);

        // Get active cart
        ServiceCart cart = getActiveCart(sessionId, user);
        log.info("Retrieved cart: {}, isEmpty: {}, cartId: {}", cart != null ? "Found" : "Not Found", 
                cart != null ? cart.isEmpty() : "N/A", cart != null ? cart.getId() : "N/A");
        
        if (cart == null || cart.isEmpty()) {
            // Debug information
            log.error("Cart validation failed - cart: {}, isEmpty: {}", cart, cart != null ? cart.isEmpty() : "N/A");
            throw new RuntimeException("Cart is empty or not found");
        }

        // Get cart items
        List<ServiceCartItem> cartItems = serviceCartItemRepository.findByCartIdWithFullDetails(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No items found in cart");
        }

        // Validate address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        // Verify address belongs to current user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to current user");
        }

        try {
            // Convert ServiceCartItems to OrderServiceRequests for existing OrderService
            List<OrderServiceRequest> orderServices = cartItems.stream().map(item -> {
                OrderServiceRequest serviceRequest = new OrderServiceRequest();
                
                // Map ServiceItem to RepairService - we'll use the ServiceItem ID as RepairService ID
                // This is a bridge between new service categories and old repair services
                serviceRequest.setServiceId(item.getServiceItem().getId());
                serviceRequest.setQuantity(item.getQuantity());
                serviceRequest.setPrice(java.math.BigDecimal.valueOf(item.getUnitPrice()));
                serviceRequest.setNotes(item.getNotes());
                
                return serviceRequest;
            }).collect(Collectors.toList());

            // Create order request using existing OrderService structure
            com.repairapp.dto.order.CreateOrderRequest orderRequest = new com.repairapp.dto.order.CreateOrderRequest();
            orderRequest.setServices(orderServices);
            orderRequest.setTotalAmount(java.math.BigDecimal.valueOf(cart.getTotalAmount()));
            orderRequest.setAddressId(addressId);
            orderRequest.setProblemDescription(problemDescription != null ? problemDescription : "Service request from cart");
            orderRequest.setCustomerNotes(customerNotes);

            // Place order using existing OrderService
            RepairOrder order = orderService.createOrderWithMultipleServices(orderRequest);

            // Clear cart after successful order placement
            clearCart(sessionId, user);

            // Prepare response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Order placed successfully");
            result.put("orderId", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("totalAmount", order.getTotalPrice());
            result.put("status", order.getStatus().getDisplayName());
            result.put("itemCount", orderServices.size());
            result.put("address", order.getPickupAddress());
            result.put("orderDate", order.getCreatedAt());

            // Add service details to response
            List<Map<String, Object>> serviceDetails = cartItems.stream().map(item -> {
                Map<String, Object> serviceDetail = new HashMap<>();
                serviceDetail.put("name", item.getServiceTitle());
                serviceDetail.put("quantity", item.getQuantity());
                serviceDetail.put("price", item.getUnitPrice());
                serviceDetail.put("totalPrice", item.getTotalPrice());
                serviceDetail.put("category", item.getCategoryName());
                return serviceDetail;
            }).collect(Collectors.toList());
            result.put("services", serviceDetails);

            log.info("Successfully placed order {} from cart for user: {}, total: {}", 
                    order.getOrderNumber(), user.getId(), order.getTotalPrice());

            return result;
        } catch (Exception e) {
            log.error("Failed to place order from cart for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to place order: " + e.getMessage(), e);
        }
    }
}
