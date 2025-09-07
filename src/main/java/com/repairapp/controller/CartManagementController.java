package com.repairapp.controller;

import com.repairapp.dto.cart.*;
import com.repairapp.dto.order.CreateOrderRequest;
import com.repairapp.dto.order.OrderServiceRequest;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.*;
import com.repairapp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Cart Management", description = "Cart and Order Management APIs for Laptop Repair Services")
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class CartManagementController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Add service to cart
     */
    @Operation(summary = "Add Service to Cart", description = "Adds a selected service to the user's cart")
    @PostMapping("/add")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CartItemDto>> addServiceToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-ADD] Adding service {} to cart for user: {}, session: {}", 
                    request.getServiceId(), currentUser.getId(), sessionId);

            CartItem cartItem = cartService.addItemToCart(
                sessionId, 
                currentUser, 
                request.getServiceId(), 
                request.getQuantity(), 
                request.getNotes()
            );

            CartItemDto cartItemDto = convertToCartItemDto(cartItem);

            log.info("[CART-ADD] Service added to cart successfully. CartItem ID: {}", cartItem.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Service added to cart successfully", cartItemDto));

        } catch (Exception e) {
            log.error("[CART-ADD] Failed to add service to cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to add service to cart: " + e.getMessage()));
        }
    }

    /**
     * Remove service from cart
     */
    @Operation(summary = "Remove Service from Cart", description = "Removes a specific service from the user's cart")
    @DeleteMapping("/remove/{cartItemId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> removeServiceFromCart(
            @PathVariable Long cartItemId,
            HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-REMOVE] Removing cart item {} for user: {}, session: {}", 
                    cartItemId, currentUser.getId(), sessionId);

            cartService.removeItemFromCart(sessionId, currentUser, cartItemId);

            log.info("[CART-REMOVE] Service removed from cart successfully. CartItem ID: {}", cartItemId);
            
            return ResponseEntity.ok(ApiResponse.success("Service removed from cart successfully"));

        } catch (Exception e) {
            log.error("[CART-REMOVE] Failed to remove service from cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to remove service from cart: " + e.getMessage()));
        }
    }

    /**
     * Get cart items
     */
    @Operation(summary = "Get Cart Items", description = "Fetches all services currently added to the user's cart")
    @GetMapping("/items")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CartSummaryDto>> getCartItems(HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-GET] Getting cart items for user: {}, session: {}", 
                    currentUser.getId(), sessionId);

            Cart cart = cartService.getActiveCart(sessionId, currentUser);
            List<CartItem> cartItems = cartService.getCartItems(sessionId, currentUser);

            List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList());

            CartSummaryDto cartSummary = new CartSummaryDto(
                cart != null ? cart.getId() : null,
                cartItemDtos,
                cartItems.size(),
                cart != null ? cart.getTotalAmount() : 0.0,
                cartItems.isEmpty(),
                cart != null ? cart.getExpiresAt() : null,
                cart != null ? cart.getUpdatedAt() : null
            );

            log.info("[CART-GET] Retrieved {} items from cart", cartItems.size());
            
            return ResponseEntity.ok(ApiResponse.success("Cart items retrieved successfully", cartSummary));

        } catch (Exception e) {
            log.error("[CART-GET] Failed to get cart items: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get cart items: " + e.getMessage()));
        }
    }

    /**
     * Place order from cart
     */
    @Operation(summary = "Place Order", description = "Places an order for all services in the cart and sends notifications")
    @PostMapping("/place-order")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<OrderConfirmationDto>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-ORDER] Placing order for user: {}, session: {}, addressId: {}", 
                    currentUser.getId(), sessionId, request.getAddressId());

            // Get cart items
            List<CartItem> cartItems = cartService.getCartItems(sessionId, currentUser);
            
            if (cartItems.isEmpty()) {
                log.warn("[CART-ORDER] Cannot place order - cart is empty for user: {}", currentUser.getId());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot place order - cart is empty"));
            }

            Cart cart = cartService.getActiveCart(sessionId, currentUser);
            
            // Create order from cart items
            RepairOrder order = createOrderFromCart(
                currentUser, 
                cartItems, 
                request.getAddressId(), 
                request.getProblemDescription(), 
                request.getCustomerNotes()
            );

            log.info("[CART-ORDER] Order created successfully: {}", order.getOrderNumber());

            // Clear the cart after successful order placement
            cartService.clearCart(sessionId, currentUser);
            log.info("[CART-ORDER] Cart cleared after order placement");

            // Send notifications
            boolean emailSent = false;
            boolean smsSent = false;

            try {
                if (request.getSendEmailNotification()) {
                    notificationService.sendOrderConfirmationEmail(order);
                    emailSent = true;
                    log.info("[CART-ORDER] Order confirmation email sent for order: {}", order.getOrderNumber());
                }

                if (request.getSendSmsNotification()) {
                    // Send SMS notification
                    String smsMessage = String.format(
                        "Order Confirmed! Order #%s with %d services worth ₹%.2f has been placed. We'll contact you soon.",
                        order.getOrderNumber(), 
                        cartItems.size(), 
                        order.getTotalPrice()
                    );
                    notificationService.sendSMS(currentUser.getPhone(), smsMessage);
                    smsSent = true;
                    log.info("[CART-ORDER] Order confirmation SMS sent for order: {}", order.getOrderNumber());
                }
            } catch (Exception notificationException) {
                log.warn("[CART-ORDER] Failed to send notifications for order {}: {}", 
                        order.getOrderNumber(), notificationException.getMessage());
            }

            // Create confirmation response
            OrderConfirmationDto confirmation = new OrderConfirmationDto(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                cartItems.size(),
                order.getStatus().getDisplayName(),
                order.getPickupAddress(),
                order.getCreatedAt(),
                String.format("Order placed successfully! Order number: %s with %d services", 
                             order.getOrderNumber(), cartItems.size()),
                emailSent,
                smsSent
            );

            log.info("[CART-ORDER] Order placement completed successfully: {}", order.getOrderNumber());
            
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully!", confirmation));

        } catch (Exception e) {
            log.error("[CART-ORDER] Failed to place order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to place order: " + e.getMessage()));
        }
    }

    /**
     * Get user order count
     */
    @Operation(summary = "Get User Order Count", description = "Returns the total number of orders placed by the user")
    @GetMapping("/order-count")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserOrderCount() {

        try {
            User currentUser = authService.getCurrentUser();

            log.info("[CART-ORDER-COUNT] Getting order count for user: {}", currentUser.getId());

            List<RepairOrder> userOrders = orderService.getUserOrders(currentUser.getId());
            
            long totalOrders = userOrders.size();
            long pendingOrders = userOrders.stream()
                .filter(order -> order.getStatus() == RepairOrder.OrderStatus.PENDING)
                .count();
            long completedOrders = userOrders.stream()
                .filter(order -> order.getStatus() == RepairOrder.OrderStatus.COMPLETED)
                .count();

            Map<String, Object> orderStats = Map.of(
                "totalOrders", totalOrders,
                "pendingOrders", pendingOrders,
                "completedOrders", completedOrders,
                "inProgressOrders", userOrders.stream()
                    .filter(order -> order.getStatus() == RepairOrder.OrderStatus.IN_PROGRESS)
                    .count(),
                "cancelledOrders", userOrders.stream()
                    .filter(order -> order.getStatus() == RepairOrder.OrderStatus.CANCELLED)
                    .count()
            );

            log.info("[CART-ORDER-COUNT] Retrieved order statistics for user: {} - Total: {}", 
                    currentUser.getId(), totalOrders);
            
            return ResponseEntity.ok(ApiResponse.success("Order count retrieved successfully", orderStats));

        } catch (Exception e) {
            log.error("[CART-ORDER-COUNT] Failed to get order count: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get order count: " + e.getMessage()));
        }
    }

    /**
     * Update cart item quantity
     */
    @Operation(summary = "Update Cart Item Quantity", description = "Updates the quantity of a specific item in the cart")
    @PutMapping("/update-quantity/{cartItemId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CartItemDto>> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-UPDATE] Updating cart item {} quantity to {} for user: {}", 
                    cartItemId, quantity, currentUser.getId());

            CartItem updatedCartItem = cartService.updateCartItemQuantity(sessionId, currentUser, cartItemId, quantity);
            CartItemDto cartItemDto = convertToCartItemDto(updatedCartItem);

            log.info("[CART-UPDATE] Cart item quantity updated successfully");
            
            return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated successfully", cartItemDto));

        } catch (Exception e) {
            log.error("[CART-UPDATE] Failed to update cart item quantity: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update cart item quantity: " + e.getMessage()));
        }
    }

    /**
     * Clear entire cart
     */
    @Operation(summary = "Clear Cart", description = "Removes all items from the user's cart")
    @DeleteMapping("/clear")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> clearCart(HttpServletRequest httpRequest) {

        try {
            User currentUser = authService.getCurrentUser();
            String sessionId = getSessionId(httpRequest);

            log.info("[CART-CLEAR] Clearing cart for user: {}, session: {}", 
                    currentUser.getId(), sessionId);

            cartService.clearCart(sessionId, currentUser);

            log.info("[CART-CLEAR] Cart cleared successfully for user: {}", currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));

        } catch (Exception e) {
            log.error("[CART-CLEAR] Failed to clear cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to clear cart: " + e.getMessage()));
        }
    }

    // Helper Methods

    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }

    private CartItemDto convertToCartItemDto(CartItem cartItem) {
        RepairService service = cartItem.getRepairService();
        return new CartItemDto(
            cartItem.getId(),
            service.getId(),
            service.getName(),
            service.getDescription(),
            service.getImageUrl(),
            cartItem.getUnitPrice(),
            cartItem.getQuantity(),
            cartItem.getTotalPrice(),
            cartItem.getNotes(),
            cartItem.getCreatedAt()
        );
    }

    private RepairOrder createOrderFromCart(User user, List<CartItem> cartItems, 
                                          Long addressId, String problemDescription, 
                                          String customerNotes) throws Exception {
        
        // Create order request from cart items
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setAddressId(addressId);
        orderRequest.setProblemDescription(problemDescription);
        orderRequest.setCustomerNotes(customerNotes);

        // Calculate total amount and create service requests
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderServiceRequest> serviceRequests = cartItems.stream()
            .map(cartItem -> {
                OrderServiceRequest serviceRequest = new OrderServiceRequest();
                serviceRequest.setServiceId(cartItem.getRepairService().getId());
                serviceRequest.setQuantity(cartItem.getQuantity());
                serviceRequest.setPrice(BigDecimal.valueOf(cartItem.getUnitPrice()));
                serviceRequest.setNotes(cartItem.getNotes());
                return serviceRequest;
            })
            .collect(Collectors.toList());

        totalAmount = cartItems.stream()
            .map(cartItem -> BigDecimal.valueOf(cartItem.getTotalPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        orderRequest.setServices(serviceRequests);
        orderRequest.setTotalAmount(totalAmount);

        // Create order using existing service
        return orderService.createOrderWithMultipleServices(orderRequest);
    }
}
