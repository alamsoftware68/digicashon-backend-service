package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.Cart;
import com.repairapp.entity.CartItem;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {
    
    private final CartService cartService;
    
    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a repair service to the cart")
    public ResponseEntity<ApiResponse> addItemToCart(
            @RequestBody @Valid AddToCartRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        CartItem cartItem = cartService.addItemToCart(
            sessionId, user, request.getServiceId(), 
            request.getQuantity(), request.getNotes());
        
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cartItem));
    }
    
    @GetMapping("/items")
    @Operation(summary = "Get cart items", description = "Retrieve all items in the current cart")
    public ResponseEntity<ApiResponse> getCartItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        List<CartItem> cartItems = cartService.getCartItems(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Cart items retrieved successfully", cartItems));
    }
    
    @GetMapping("/summary")
    @Operation(summary = "Get cart summary", description = "Get cart summary with totals and item count")
    public ResponseEntity<ApiResponse> getCartSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        int itemCount = cartService.getCartItemCount(sessionId, user);
        double total = cartService.getCartTotal(sessionId, user);
        
        Map<String, Object> summary = Map.of(
            "itemCount", itemCount,
            "totalAmount", total,
            "isEmpty", itemCount == 0
        );
        
        return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully", summary));
    }
    
    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific cart item")
    public ResponseEntity<ApiResponse> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateQuantityRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        CartItem updatedItem = cartService.updateCartItemQuantity(
            sessionId, user, cartItemId, request.getQuantity());
        
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated successfully", updatedItem));
    }
    
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the cart")
    public ResponseEntity<ApiResponse> removeItemFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        cartService.removeItemFromCart(sessionId, user, cartItemId);
        
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully"));
    }
    
    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    public ResponseEntity<ApiResponse> clearCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        cartService.clearCart(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
    
    @PostMapping("/merge")
    @Operation(summary = "Merge session cart with user cart", description = "Merge guest cart with user cart after login")
    public ResponseEntity<ApiResponse> mergeCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        if (userPrincipal == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User must be authenticated to merge cart"));
        }
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        Cart mergedCart = cartService.mergeSessionCartWithUserCart(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Cart merged successfully", mergedCart));
    }
    
    @GetMapping("/check-service/{serviceId}")
    @Operation(summary = "Check if service is in cart", description = "Check if a specific service is already in the cart")
    public ResponseEntity<ApiResponse> isServiceInCart(
            @PathVariable Long serviceId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        boolean isInCart = cartService.isServiceInCart(sessionId, user, serviceId);
        
        return ResponseEntity.ok(ApiResponse.success("Service check completed", 
            Map.of("serviceId", serviceId, "isInCart", isInCart)));
    }
    
    @PostMapping("/extend-expiry/{cartId}")
    @Operation(summary = "Extend cart expiry", description = "Extend the expiry time of a cart")
    public ResponseEntity<ApiResponse> extendCartExpiry(
            @PathVariable Long cartId,
            @RequestParam(defaultValue = "24") int hours,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
        
        // Validate cart ownership
        if (!cartService.validateCartOwnership(cartId, sessionId, user)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You don't have permission to modify this cart"));
        }
        
        cartService.extendCartExpiry(cartId, hours);
        
        return ResponseEntity.ok(ApiResponse.success("Cart expiry extended successfully"));
    }
    
    // Helper method to get session ID
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }
    
    // Helper method to create User from UserPrincipal
    private User getUserFromPrincipal(UserPrincipal userPrincipal) {
        User user = new User();
        user.setId(userPrincipal.getId());
        user.setPhone(userPrincipal.getPhone());
        user.setEmail(userPrincipal.getEmail());
        user.setName(userPrincipal.getName());
        user.setRole(userPrincipal.getRole());
        user.setIsActive(userPrincipal.getIsActive());
        return user;
    }
    
    // DTOs
    public static class AddToCartRequest {
        @NotNull(message = "Service ID is required")
        private Long serviceId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        private String notes;
        
        // Getters and setters
        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class UpdateQuantityRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        // Getters and setters
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
