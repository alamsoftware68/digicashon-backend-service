package com.repairapp.service.impl;

import com.repairapp.entity.Cart;
import com.repairapp.entity.CartItem;
import com.repairapp.entity.RepairService;
import com.repairapp.entity.User;
import com.repairapp.exception.BadRequestException;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.CartItemRepository;
import com.repairapp.repository.CartRepository;
import com.repairapp.repository.RepairServiceRepository;
import com.repairapp.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RepairServiceRepository repairServiceRepository;
    
    @Override
    public Cart getOrCreateCart(String sessionId, User user) {
        Cart existingCart = getActiveCart(sessionId, user);
        
        if (existingCart != null) {
            Cart cart = existingCart;
            // Extend expiry if cart is close to expiring
            if (cart.getExpiresAt().isBefore(LocalDateTime.now().plusHours(1))) {
                cart.extendExpiry(user != null ? 24 : 2);
                return cartRepository.save(cart);
            }
            return cart;
        }
        
        // Create new cart
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setSessionId(user == null ? sessionId : null);
        newCart.setIsActive(true);
        newCart.setTotalAmount(0.0);
        newCart.setItemCount(0);
        
        return cartRepository.save(newCart);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Cart getActiveCart(String sessionId, User user) {
        if (user != null) {
            return cartRepository.findByUserAndIsActiveTrue(user).orElse(null);
        } else if (sessionId != null) {
            return cartRepository.findBySessionIdAndIsActiveTrue(sessionId).orElse(null);
        }
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }
    
    @Override
    public void clearCart(String sessionId, User user) {
        Cart cart = getActiveCart(sessionId, user);
        if (cart != null) {
            cart.clearItems();
            cart.setTotalAmount(0.0);
            cart.setItemCount(0);
            cartRepository.save(cart);
            log.info("Cleared cart for session: {} or user: {}", sessionId, user != null ? user.getId() : null);
        }
    }
    
    @Override
    public void deactivateCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        cart.setIsActive(false);
        cartRepository.save(cart);
        log.info("Deactivated cart with ID: {}", cartId);
    }
    
    @Override
    public CartItem addItemToCart(String sessionId, User user, Long serviceId, Integer quantity, String notes) {
        validateCartItemQuantity(quantity);
        
        RepairService repairService = repairServiceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));
        
        validateServiceForCart(repairService);
        
        Cart cart = getOrCreateCart(sessionId, user);
        
        // Check if service already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndRepairService(cart, repairService);
        
        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + quantity);
            if (notes != null && !notes.trim().isEmpty()) {
                item.setNotes(notes);
            }
            CartItem savedItem = cartItemRepository.save(item);
            refreshCartTotals(cart.getId());
            log.info("Updated existing cart item with service ID: {} for cart: {}", serviceId, cart.getId());
            return savedItem;
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setRepairService(repairService);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(repairService.getBasePrice().doubleValue());
            newItem.setNotes(notes);
            newItem.calculateTotalPrice();
            
            CartItem savedItem = cartItemRepository.save(newItem);
            refreshCartTotals(cart.getId());
            log.info("Added new cart item with service ID: {} to cart: {}", serviceId, cart.getId());
            return savedItem;
        }
    }
    
    @Override
    public CartItem updateCartItemQuantity(String sessionId, User user, Long cartItemId, Integer quantity) {
        validateCartItemQuantity(quantity);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        // Verify ownership
        if (!validateCartOwnership(cartItem.getCart().getId(), sessionId, user)) {
            throw new BadRequestException("You don't have permission to modify this cart item");
        }
        
        cartItem.updateQuantity(quantity);
        CartItem savedItem = cartItemRepository.save(cartItem);
        refreshCartTotals(cartItem.getCart().getId());
        
        log.info("Updated cart item quantity for item ID: {} to quantity: {}", cartItemId, quantity);
        return savedItem;
    }
    
    @Override
    public void removeItemFromCart(String sessionId, User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        // Verify ownership
        if (!validateCartOwnership(cartItem.getCart().getId(), sessionId, user)) {
            throw new BadRequestException("You don't have permission to remove this cart item");
        }
        
        Long cartId = cartItem.getCart().getId();
        cartItemRepository.delete(cartItem);
        refreshCartTotals(cartId);
        
        log.info("Removed cart item with ID: {} from cart: {}", cartItemId, cartId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(String sessionId, User user) {
        Cart cart = getActiveCart(sessionId, user);
        if (cart == null) {
            return List.of();
        }
        return cartItemRepository.findByCartOrderByCreatedAtAsc(cart);
    }
    
    @Override
    public Cart refreshCartTotals(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        recalculateCartTotals(cart);
        return cartRepository.save(cart);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isServiceInCart(String sessionId, User user, Long serviceId) {
        Cart cart = getActiveCart(sessionId, user);
        if (cart == null) {
            return false;
        }
        
        RepairService service = repairServiceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + serviceId));
        
        return cartItemRepository.existsByCartAndRepairService(cart, service);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(String sessionId, User user) {
        Cart cart = getActiveCart(sessionId, user);
        return cart != null ? cart.getItemCount() : 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public double getCartTotal(String sessionId, User user) {
        Cart cart = getActiveCart(sessionId, user);
        return cart != null ? cart.getTotalAmount() : 0.0;
    }
    
    @Override
    public Cart transferSessionCartToUser(String sessionId, User user) {
        if (sessionId == null || user == null) {
            throw new BadRequestException("Session ID and User are required for cart transfer");
        }
        
        Optional<Cart> sessionCart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId);
        if (sessionCart.isEmpty()) {
            return null;
        }
        
        Cart cart = sessionCart.get();
        cart.setUser(user);
        cart.setSessionId(null);
        
        Cart savedCart = cartRepository.save(cart);
        log.info("Transferred cart from session: {} to user: {}", sessionId, user.getId());
        return savedCart;
    }
    
    @Override
    public Cart mergeSessionCartWithUserCart(String sessionId, User user) {
        if (sessionId == null || user == null) {
            throw new BadRequestException("Session ID and User are required for cart merging");
        }
        
        Optional<Cart> sessionCart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId);
        Optional<Cart> userCart = cartRepository.findByUserAndIsActiveTrue(user);
        
        if (sessionCart.isEmpty()) {
            return userCart.orElse(null);
        }
        
        if (userCart.isEmpty()) {
            return transferSessionCartToUser(sessionId, user);
        }
        
        // Both carts exist - merge session cart into user cart
        Cart targetCart = userCart.get();
        Cart sourceCart = sessionCart.get();
        
        for (CartItem sessionItem : sourceCart.getCartItems()) {
            Optional<CartItem> existingItem = cartItemRepository.findByCartAndRepairService(
                targetCart, sessionItem.getRepairService());
            
            if (existingItem.isPresent()) {
                // Combine quantities
                CartItem existing = existingItem.get();
                existing.updateQuantity(existing.getQuantity() + sessionItem.getQuantity());
                cartItemRepository.save(existing);
            } else {
                // Move item to target cart
                sessionItem.setCart(targetCart);
                cartItemRepository.save(sessionItem);
            }
        }
        
        // Deactivate session cart
        sourceCart.setIsActive(false);
        cartRepository.save(sourceCart);
        
        Cart mergedCart = refreshCartTotals(targetCart.getId());
        log.info("Merged session cart: {} into user cart: {} for user: {}", 
            sourceCart.getId(), targetCart.getId(), user.getId());
        
        return mergedCart;
    }
    
    @Override
    public void extendCartExpiry(Long cartId, int hours) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        cart.extendExpiry(hours);
        cartRepository.save(cart);
        log.info("Extended cart expiry for cart ID: {} by {} hours", cartId, hours);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateCartOwnership(Long cartId, String sessionId, User user) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        return cart.isOwnedBy(sessionId, user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCartExpired(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        return cart.isExpired();
    }
    
    @Override
    public void validateCartItemQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (quantity > 10) {
            throw new BadRequestException("Maximum quantity per item is 10");
        }
    }
    
    @Override
    public void validateServiceForCart(RepairService service) {
        if (service == null) {
            throw new BadRequestException("Service cannot be null");
        }
        if (!service.getIsActive()) {
            throw new BadRequestException("Service is not active and cannot be added to cart");
        }
        if (service.getBasePrice() == null || service.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Service must have a valid price");
        }
    }
    
    @Override
    public void cleanupExpiredCarts() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findExpiredCarts(currentTime);
        
        for (Cart cart : expiredCarts) {
            cart.setIsActive(false);
        }
        
        if (!expiredCarts.isEmpty()) {
            cartRepository.saveAll(expiredCarts);
            log.info("Deactivated {} expired carts", expiredCarts.size());
        }
    }
    
    @Override
    public void cleanupInactiveCarts(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = cartRepository.deleteOldInactiveCarts(cutoffTime);
        log.info("Deleted {} inactive carts older than {} days", deletedCount, daysOld);
    }
    
    @Override
    public void cleanupEmptyCarts() {
        // Find and deactivate empty carts that are older than 1 hour
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<Cart> allCarts = cartRepository.findAll();
        
        List<Cart> emptyCarts = allCarts.stream()
            .filter(cart -> cart.getIsActive() && 
                          cart.getItemCount() == 0 && 
                          cart.getCreatedAt().isBefore(cutoffTime))
            .toList();
        
        for (Cart cart : emptyCarts) {
            cart.setIsActive(false);
        }
        
        if (!emptyCarts.isEmpty()) {
            cartRepository.saveAll(emptyCarts);
            log.info("Deactivated {} empty carts", emptyCarts.size());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Cart> getRecentCarts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return cartRepository.findRecentCarts(since);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Cart> getAbandonedCarts(int hoursOld) {
        LocalDateTime abandonedTime = LocalDateTime.now().minusHours(hoursOld);
        return cartRepository.findAbandonedCarts(abandonedTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getActiveCartsCount() {
        return cartRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCartEmpty(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        
        return cart.isEmpty();
    }
    
    @Override
    public void recalculateCartTotals(Cart cart) {
        if (cart == null) {
            throw new BadRequestException("Cart cannot be null");
        }
        
        // Recalculate from database to ensure accuracy
        Double totalPrice = cartItemRepository.getTotalPriceInCart(cart);
        Integer totalQuantity = cartItemRepository.getTotalQuantityInCart(cart);
        long itemCount = cartItemRepository.countByCart(cart);
        
        cart.setTotalAmount(totalPrice != null ? totalPrice : 0.0);
        cart.setItemCount((int) itemCount);
        
        log.debug("Recalculated cart totals for cart ID: {}, Total: {}, Items: {}", 
            cart.getId(), cart.getTotalAmount(), cart.getItemCount());
    }
    
    @Override
    public Cart saveCart(Cart cart) {
        if (cart == null) {
            throw new BadRequestException("Cart cannot be null");
        }
        return cartRepository.save(cart);
    }
    
    // Helper method to generate session ID if needed
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    // Additional utility method for cart validation
    private Cart getActiveCartInternal(String sessionId, User user) {
        Cart cart = getActiveCart(sessionId, user);
        if (cart == null) {
            throw new ResourceNotFoundException("No active cart found for the session or user");
        }
        
        if (cart.isExpired()) {
            cart.setIsActive(false);
            cartRepository.save(cart);
            throw new BadRequestException("Cart has expired. Please create a new cart.");
        }
        
        return cart;
    }
}
