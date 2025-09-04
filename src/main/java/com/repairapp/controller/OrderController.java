package com.repairapp.controller;

import com.repairapp.dto.order.CreateOrderRequest;
import com.repairapp.dto.order.CreateOrderWithOTPRequest;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.OrderItem;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.AuthService;
import com.repairapp.service.CartService;
import com.repairapp.service.OTPService;
import com.repairapp.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order management REST controller
 */
@Tag(name = "Order Management", description = "Repair order management APIs")
@RestController
@RequestMapping("/orders")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;
    
    @Autowired
    private OTPService otpService;
    
    @Autowired
    private CartService cartService;

    /**
     * Create new repair order with OTP verification
     */
    @Operation(summary = "Create order with OTP verification", description = "Create a new repair order with OTP verification")
    @PostMapping("/create-with-otp")
    public ResponseEntity<ApiResponse> createOrderWithOTP(
            @Valid @RequestBody CreateOrderWithOTPRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        
        // Verify OTP first
        boolean otpValid = otpService.verifyOTP(
            request.getContactIdentifier(),
            request.getOtpCode(),
            OTPVerification.OTPType.ORDER_CONFIRMATION);
        
        if (!otpValid) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid or expired OTP. Please verify your contact information."));
        }
        
        // Create order
        RepairOrder order = orderService.createOrderWithMultipleServices(request.toBasicRequest());
        
        // Clear cart after successful order creation
        try {
            String sessionId = getSessionId(httpRequest);
            User user = userPrincipal != null ? getUserFromPrincipal(userPrincipal) : null;
            cartService.clearCart(sessionId, user);
        } catch (Exception e) {
            // Log but don't fail the order creation
            System.err.println("Failed to clear cart after order creation: " + e.getMessage());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Order created successfully with OTP verification", order));
    }

    /**
     * Create new repair order with multiple services (Frontend Compatible)
     */
    @Operation(summary = "Create order with multiple services", description = "Create a new repair order with multiple services")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RepairOrder>> createOrderWithServices(@Valid @RequestBody CreateOrderRequest request) {
        RepairOrder order = orderService.createOrderWithMultipleServices(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    /**
     * Create new repair order
     */
    @Operation(summary = "Create order", description = "Create a new repair order")
    @PostMapping
    public ResponseEntity<ApiResponse<RepairOrder>> createOrder(
            @RequestParam Long serviceId,
            @RequestParam String problemDescription,
            @RequestParam String pickupAddress,
            @RequestParam(required = false) String customerNotes) {
        
        RepairOrder order = orderService.createOrder(serviceId, problemDescription, pickupAddress, customerNotes);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    /**
     * Get current user's orders
     */
    @Operation(summary = "Get user orders", description = "Get all orders for the current user")
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<RepairOrder>>> getMyOrders() {
        User currentUser = authService.getCurrentUser();
        List<RepairOrder> orders = orderService.getUserOrders(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Get current user's orders with pagination
     */
    @Operation(summary = "Get user orders (paginated)", description = "Get user orders with pagination")
    @GetMapping("/my-orders/page")
    public ResponseEntity<ApiResponse<Page<RepairOrder>>> getMyOrdersWithPagination(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<RepairOrder> orders = orderService.getUserOrdersWithPagination(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Get order by ID
     */
    @Operation(summary = "Get order by ID", description = "Get a specific order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<RepairOrder>> getOrderById(@PathVariable Long orderId) {
        RepairOrder order = orderService.getOrderById(orderId);
        
        // Check if user has access to this order
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() == User.Role.CUSTOMER && 
            !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this order"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    /**
     * Get order by order number
     */
    @Operation(summary = "Get order by number", description = "Get order by order number")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<RepairOrder>> getOrderByNumber(@PathVariable String orderNumber) {
        RepairOrder order = orderService.getOrderByOrderNumber(orderNumber);
        
        // Check if user has access to this order
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() == User.Role.CUSTOMER && 
            !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this order"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    /**
     * Update order status (technicians and admin only)
     */
    @Operation(summary = "Update order status", description = "Update order status and add technician notes")
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('TECHNICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairOrder>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam RepairOrder.OrderStatus status,
            @RequestParam(required = false) String technicianNotes) {
        
        RepairOrder order = orderService.updateOrderStatus(orderId, status, technicianNotes);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    /**
     * Assign technician to order (admin only)
     */
    @Operation(summary = "Assign technician", description = "Assign a technician to an order")
    @PutMapping("/{orderId}/assign-technician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairOrder>> assignTechnician(
            @PathVariable Long orderId,
            @RequestParam Long technicianId) {
        
        RepairOrder order = orderService.assignTechnician(orderId, technicianId);
        return ResponseEntity.ok(ApiResponse.success("Technician assigned successfully", order));
    }

    /**
     * Schedule order
     */
    @Operation(summary = "Schedule order", description = "Set scheduled date for an order")
    @PutMapping("/{orderId}/schedule")
    @PreAuthorize("hasRole('TECHNICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairOrder>> scheduleOrder(
            @PathVariable Long orderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledDate) {
        
        RepairOrder order = orderService.scheduleOrder(orderId, scheduledDate);
        return ResponseEntity.ok(ApiResponse.success("Order scheduled successfully", order));
    }

    /**
     * Cancel order
     */
    @Operation(summary = "Cancel order", description = "Cancel an existing order")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<RepairOrder>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        
        RepairOrder order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    /**
     * Cancel order by order number (Frontend Compatible)
     */
    @Operation(summary = "Cancel order by number", description = "Cancel order by order number")
    @PutMapping("/cancel/{orderNumber}")
    public ResponseEntity<ApiResponse<RepairOrder>> cancelOrderByNumber(
            @PathVariable String orderNumber,
            @RequestParam(required = false, defaultValue = "Cancelled by customer") String reason) {
        
        RepairOrder order = orderService.cancelOrderByNumber(orderNumber, reason);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    /**
     * Get order items for a specific order
     */
    @Operation(summary = "Get order items", description = "Get all items/services for a specific order")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order items retrieved successfully", orderItems));
    }

    /**
     * Get all orders (admin only)
     */
    @Operation(summary = "Get all orders", description = "Get all orders (admin access)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RepairOrder>>> getAllOrders() {
        List<RepairOrder> orders = orderService.getRecentOrders();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Get orders by status (technician/admin)
     */
    @Operation(summary = "Get orders by status", description = "Get orders filtered by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('TECHNICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RepairOrder>>> getOrdersByStatus(@PathVariable RepairOrder.OrderStatus status) {
        List<RepairOrder> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Search orders
     */
    @Operation(summary = "Search orders", description = "Search orders by order number or service name")
    @GetMapping("/search")
    @PreAuthorize("hasRole('TECHNICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RepairOrder>>> searchOrders(@RequestParam String q) {
        List<RepairOrder> orders = orderService.searchOrders(q);
        return ResponseEntity.ok(ApiResponse.success("Search completed", orders));
    }

    /**
     * Get unassigned orders (admin only)
     */
    @Operation(summary = "Get unassigned orders", description = "Get orders that need technician assignment")
    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RepairOrder>>> getUnassignedOrders() {
        List<RepairOrder> orders = orderService.getUnassignedOrders();
        return ResponseEntity.ok(ApiResponse.success("Unassigned orders retrieved successfully", orders));
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
}
