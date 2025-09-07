package com.digicashon.digicashonbackendservice.controller;

import com.digicashon.digicashonbackendservice.dto.ServiceCartDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCartItemDto;
import com.digicashon.digicashonbackendservice.entity.ServiceCart;
import com.digicashon.digicashonbackendservice.entity.ServiceCartItem;
import com.digicashon.digicashonbackendservice.service.ServiceCartService;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Service Cart management operations
 */
@Tag(name = "Service Cart Management", description = "Service Cart and Cart Item management APIs")
@RestController
@RequestMapping("/service-cart")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class ServiceCartController {

    private final ServiceCartService serviceCartService;
    private final AuthService authService;

    // ============== CART MANAGEMENT ENDPOINTS ==============

    /**
     * Get current user's cart
     */
    @Operation(summary = "Get cart", description = "Retrieve the current user's active cart")
    @GetMapping
    public ResponseEntity<ApiResponse<ServiceCartDto>> getCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        ServiceCartDto cartDto = serviceCartService.getCartDto(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cartDto));
    }

    /**
     * Get cart summary with item count and total
     */
    @Operation(summary = "Get cart summary", description = "Get cart summary including item count and total amount")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCartSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        Map<String, Object> summary = serviceCartService.getCartSummary(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully", summary));
    }

    /**
     * Clear cart - remove all items
     */
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.clearCart(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", "Cart is now empty"));
    }

    // ============== CART ITEM ENDPOINTS ==============

    /**
     * Get all cart items
     */
    @Operation(summary = "Get cart items", description = "Get all items in the current cart")
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ServiceCartItemDto>>> getCartItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        List<ServiceCartItemDto> items = serviceCartService.getCartItemsDto(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart items retrieved successfully", items));
    }

    /**
     * Add service item to cart
     */
    @Operation(summary = "Add item to cart", description = "Add a service item to the cart")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<ServiceCartItemDto>> addItemToCart(
            @Valid @RequestBody AddItemRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        ServiceCartItem cartItem = serviceCartService.addItemToCart(
                sessionId, user, request.getServiceItemId(), request.getQuantity(), request.getNotes());
        
        ServiceCartItemDto cartItemDto = ServiceCartItemDto.fromEntity(cartItem);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cartItemDto));
    }

    /**
     * Update cart item quantity
     */
    @Operation(summary = "Update cart item", description = "Update the quantity of an item in the cart")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<ServiceCartItemDto>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateItemRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        ServiceCartItem cartItem = serviceCartService.updateCartItemQuantity(
                sessionId, user, cartItemId, request.getQuantity());
        
        ServiceCartItemDto cartItemDto = ServiceCartItemDto.fromEntity(cartItem);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cartItemDto));
    }

    /**
     * Remove specific cart item
     */
    @Operation(summary = "Remove cart item", description = "Remove a specific item from the cart")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.removeItemFromCart(sessionId, user, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", "Item removed"));
    }

    /**
     * Remove service from cart (by service item ID)
     */
    @Operation(summary = "Remove service from cart", description = "Remove all instances of a service from the cart")
    @DeleteMapping("/services/{serviceItemId}")
    public ResponseEntity<ApiResponse<String>> removeServiceFromCart(
            @PathVariable Long serviceItemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.removeServiceFromCart(sessionId, user, serviceItemId);
        return ResponseEntity.ok(ApiResponse.success("Service removed from cart successfully", "Service removed"));
    }

    // ============== FRONTEND INTEGRATION ENDPOINTS ==============

    /**
     * Get cart for frontend (formatted for UI consumption)
     */
    @Operation(summary = "Get cart for frontend", description = "Get cart data formatted for frontend consumption")
    @GetMapping("/frontend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCartForFrontend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        List<Map<String, Object>> cartData = serviceCartService.getCartForFrontend(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart data retrieved for frontend", cartData));
    }

    /**
     * Add service for frontend (with enhanced response)
     */
    @Operation(summary = "Add service for frontend", description = "Add service to cart with frontend-friendly response")
    @PostMapping("/frontend/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addServiceForFrontend(
            @Valid @RequestBody AddServiceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        Map<String, Object> result = serviceCartService.addServiceForFrontend(
                sessionId, user, request.getServiceItemId(), request.getQuantity(), request.getNotes());
        
        return ResponseEntity.ok(ApiResponse.success("Service added to cart", result));
    }

    /**
     * Remove service for frontend
     */
    @Operation(summary = "Remove service for frontend", description = "Remove service from cart (frontend endpoint)")
    @DeleteMapping("/frontend/remove/{serviceItemId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeServiceForFrontend(
            @PathVariable Long serviceItemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.removeServiceForFrontend(sessionId, user, serviceItemId);
        Map<String, Object> summary = serviceCartService.getCartSummaryForFrontend(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Service removed from cart", summary));
    }

    // ============== ORDER PLACEMENT ENDPOINT ==============

    /**
     * Place order from cart (Book Now functionality)
     */
    @Operation(summary = "Place order from cart", description = "Convert cart items to order (Book Now functionality)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeOrderFromCart(
            @Valid @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required to place order"));
        }
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        try {
            // Place order using cart items
            Map<String, Object> orderResult = serviceCartService.placeOrderFromCart(
                    sessionId, user, request.getAddressId(), request.getProblemDescription(), request.getCustomerNotes());
            
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully", orderResult));
        } catch (Exception e) {
            log.error("Failed to place order from cart for user: {}", user.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to place order: " + e.getMessage()));
        }
    }

    // ============== UTILITY ENDPOINTS ==============

    /**
     * Check if service is in cart
     */
    @Operation(summary = "Check if service in cart", description = "Check if a specific service is already in the cart")
    @GetMapping("/check/{serviceItemId}")
    public ResponseEntity<ApiResponse<Boolean>> isServiceInCart(
            @PathVariable Long serviceItemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        boolean inCart = serviceCartService.isServiceInCart(sessionId, user, serviceItemId);
        return ResponseEntity.ok(ApiResponse.success("Service check completed", inCart));
    }

    /**
     * Get cart item count
     */
    @Operation(summary = "Get cart item count", description = "Get the number of items in the cart")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        int count = serviceCartService.getCartItemCount(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart item count retrieved", count));
    }

    /**
     * Get cart total
     */
    @Operation(summary = "Get cart total", description = "Get the total amount of items in the cart")
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<Double>> getCartTotal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        double total = serviceCartService.getCartTotal(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success("Cart total retrieved", total));
    }

    // ============== SESSION MANAGEMENT ENDPOINTS ==============

    /**
     * Transfer session cart to user (called during login)
     */
    @Operation(summary = "Transfer session cart", description = "Transfer guest cart to authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<ServiceCartDto>> transferSessionCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        String sessionId = getSessionId(request);
        User user = getUserFromPrincipal(userPrincipal);
        
        ServiceCart cart = serviceCartService.mergeSessionCartWithUserCart(sessionId, user);
        ServiceCartDto cartDto = ServiceCartDto.fromEntity(cart);
        
        return ResponseEntity.ok(ApiResponse.success("Session cart transferred successfully", cartDto));
    }

    // ============== BULK OPERATIONS ==============

    /**
     * Add multiple items to cart
     */
    @Operation(summary = "Add multiple items", description = "Add multiple items to cart in one request")
    @PostMapping("/bulk/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMultipleItems(
            @Valid @RequestBody BulkAddItemsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.addMultipleItems(sessionId, user, request.getItems());
        Map<String, Object> summary = serviceCartService.getCartSummary(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Multiple items added successfully", summary));
    }

    /**
     * Remove multiple items from cart
     */
    @Operation(summary = "Remove multiple items", description = "Remove multiple items from cart")
    @DeleteMapping("/bulk/remove")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeMultipleItems(
            @RequestBody BulkRemoveItemsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        String sessionId = getSessionId(httpRequest);
        User user = getUserFromPrincipal(userPrincipal);
        
        serviceCartService.removeMultipleItems(sessionId, user, request.getServiceItemIds());
        Map<String, Object> summary = serviceCartService.getCartSummary(sessionId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Multiple items removed successfully", summary));
    }

    // ============== HELPER METHODS ==============

    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }

    private User getUserFromPrincipal(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return null;
        }
        
        User user = new User();
        user.setId(userPrincipal.getId());
        user.setPhone(userPrincipal.getPhone());
        user.setEmail(userPrincipal.getEmail());
        user.setName(userPrincipal.getName());
        user.setRole(userPrincipal.getRole());
        user.setIsActive(userPrincipal.getIsActive());
        // Fix: Set version to 0 for manually created User entities to avoid optimistic locking issues
        user.setVersion(0L);
        return user;
    }

    // ============== REQUEST DTOs ==============

    /**
     * Request DTO for adding item to cart
     */
    public static class AddItemRequest {
        private Long serviceItemId;
        private Integer quantity = 1;
        private String notes;

        // Getters and setters
        public Long getServiceItemId() { return serviceItemId; }
        public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Request DTO for updating item quantity
     */
    public static class UpdateItemRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * Request DTO for adding service (frontend)
     */
    public static class AddServiceRequest {
        private Long serviceItemId;
        private Integer quantity = 1;
        private String notes;

        public Long getServiceItemId() { return serviceItemId; }
        public void setServiceItemId(Long serviceItemId) { this.serviceItemId = serviceItemId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Request DTO for placing order from cart
     */
    public static class PlaceOrderRequest {
        private Long addressId;
        private String problemDescription;
        private String customerNotes;

        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }
        
        public String getProblemDescription() { return problemDescription; }
        public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
        
        public String getCustomerNotes() { return customerNotes; }
        public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
    }

    /**
     * Request DTO for bulk adding items
     */
    public static class BulkAddItemsRequest {
        private List<Map<String, Object>> items;

        public List<Map<String, Object>> getItems() { return items; }
        public void setItems(List<Map<String, Object>> items) { this.items = items; }
    }

    /**
     * Request DTO for bulk removing items
     */
    public static class BulkRemoveItemsRequest {
        private List<Long> serviceItemIds;

        public List<Long> getServiceItemIds() { return serviceItemIds; }
        public void setServiceItemIds(List<Long> serviceItemIds) { this.serviceItemIds = serviceItemIds; }
    }
}
