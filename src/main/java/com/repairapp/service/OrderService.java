package com.repairapp.service;

import com.repairapp.dto.order.CreateOrderRequest;
import com.repairapp.dto.order.OrderServiceRequest;
import com.repairapp.entity.*;
import com.repairapp.exception.BadRequestException;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.*;
import com.repairapp.util.OrderNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for order management operations
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private RepairOrderRepository orderRepository;

    @Autowired
    private RepairServiceRepository serviceRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private com.digicashon.digicashonbackendservice.repository.ServiceItemRepository serviceItemRepository;

    /**
     * *** ENHANCED: Create order with cart item details preserved ***
     */
    public RepairOrder createOrderWithMultipleServicesFromCart(CreateOrderRequest request, 
            List<CartItemDetails> cartItemDetails) {
        User currentUser = authService.getCurrentUser();
        
        // Validate address
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
        
        // Verify address belongs to current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Address does not belong to current user");
        }
        
        // Generate unique order number
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        
        // Create order
        RepairOrder order = new RepairOrder();
        order.setOrderNumber(orderNumber);
        order.setUser(currentUser);
        order.setTotalPrice(request.getTotalAmount());
        order.setProblemDescription(request.getProblemDescription());
        order.setPickupAddress(address.getFullAddress());
        order.setCustomerNotes(request.getCustomerNotes());
        order.setStatus(RepairOrder.OrderStatus.PENDING);
        order.setAddress(address);
        
        // Save order first to get ID
        RepairOrder savedOrder = orderRepository.save(order);
        
        // Create order items with ACTUAL CART ITEM DETAILS
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (int i = 0; i < request.getServices().size(); i++) {
            OrderServiceRequest serviceRequest = request.getServices().get(i);
            CartItemDetails cartDetails = (cartItemDetails != null && i < cartItemDetails.size()) ? cartItemDetails.get(i) : null;
            
            RepairService service = findOrCreateRepairService(serviceRequest.getServiceId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setService(service);
            orderItem.setQuantity(serviceRequest.getQuantity());
            orderItem.setUnitPrice(serviceRequest.getPrice());
            orderItem.setTotalPrice(serviceRequest.getPrice().multiply(BigDecimal.valueOf(serviceRequest.getQuantity())));
            orderItem.setNotes(serviceRequest.getNotes());
            
            // *** CRITICAL FIX: Store actual cart item details ***
            if (cartDetails != null) {
                orderItem.setServiceName(cartDetails.getServiceName()); // "MacBook"
                orderItem.setServiceCategory(cartDetails.getCategoryName()); // "Home Visit"
                logger.info("✅ FIXED: Storing cart item details - Name: {}, Category: {}", 
                           cartDetails.getServiceName(), cartDetails.getCategoryName());
            } else {
                // Fallback to service data if cart details not available
                orderItem.setServiceName(service.getName());
                orderItem.setServiceCategory(service.getDeviceType().toString());
                logger.warn("Using service fallback data for order item");
            }
            
            orderItemRepository.save(orderItem);
            calculatedTotal = calculatedTotal.add(orderItem.getTotalPrice());
        }
        
        // Set the first service as the main service for compatibility
        if (!request.getServices().isEmpty()) {
            RepairService firstService = findOrCreateRepairService(request.getServices().get(0).getServiceId());
            savedOrder.setService(firstService);
        }
        
        // Update order with final data
        savedOrder = orderRepository.save(savedOrder);
        
        // Create invoice for the order
        createInvoiceForOrder(savedOrder);
        
        logger.info("✅ FIXED: Order created with actual cart item names: {} for user: {} with {} services", 
                   orderNumber, currentUser.getPhone(), request.getServices().size());
        return savedOrder;
    }

    /**
     * *** ENHANCED: Create new repair order with multiple services and proper category information ***
     */
    public RepairOrder createOrderWithMultipleServices(CreateOrderRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Validate address
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
        
        // Verify address belongs to current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Address does not belong to current user");
        }
        
        // Generate unique order number
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        
        // Create order
        RepairOrder order = new RepairOrder();
        order.setOrderNumber(orderNumber);
        order.setUser(currentUser);
        order.setTotalPrice(request.getTotalAmount());
        order.setProblemDescription(request.getProblemDescription());
        order.setPickupAddress(address.getFullAddress());
        order.setCustomerNotes(request.getCustomerNotes());
        order.setStatus(RepairOrder.OrderStatus.PENDING);
        order.setAddress(address);
        
        // Save order first to get ID
        RepairOrder savedOrder = orderRepository.save(order);
        
        // Create order items with PROPER CATEGORY AND SERVICE ITEM DETAILS
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<OrderItem> createdOrderItems = new ArrayList<>(); // *** TRACK CREATED ITEMS ***
        
        for (OrderServiceRequest serviceRequest : request.getServices()) {
            
            // *** ENHANCED: Fetch actual service item and category information ***
            com.digicashon.digicashonbackendservice.entity.ServiceItem serviceItem = null;
            if (serviceRequest.getServiceId() != null) {
                serviceItem = serviceItemRepository.findById(serviceRequest.getServiceId())
                    .orElse(null);
            }
            
            RepairService service = findOrCreateRepairService(serviceRequest.getServiceId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setService(service);
            orderItem.setQuantity(serviceRequest.getQuantity());
            orderItem.setUnitPrice(serviceRequest.getPrice());
            orderItem.setTotalPrice(serviceRequest.getPrice().multiply(BigDecimal.valueOf(serviceRequest.getQuantity())));
            orderItem.setNotes(serviceRequest.getNotes());
            
            // *** CRITICAL ENHANCEMENT: Store proper category and service information ***
            if (serviceItem != null) {
                // Get category name from service_categories table
                String categoryName = serviceItem.getServiceCategory().getCategoryName();
                String serviceTitle = serviceItem.getServiceTitle();
                
                orderItem.setServiceItemId(serviceItem.getId());
                orderItem.setServiceCategory(categoryName); // "Home Visit", "Repair", "Laptop/Desktop", "Add-On"
                orderItem.setServiceTitle(serviceTitle); // "Windows Laptop", "Desktop", "MacBook"
                
                // Use provided service name or fall back to service title
                orderItem.setServiceName(serviceRequest.getName() != null ? 
                    serviceRequest.getName() : serviceTitle);
                
                logger.info("✅ ENHANCED: Storing service details - Category: '{}', Title: '{}', Name: '{}', ItemId: {}", 
                           categoryName, serviceTitle, orderItem.getServiceName(), serviceItem.getId());
                           
                // *** DEBUG: Log what we're setting ***
                logger.info("🔍 DEBUG: OrderItem after setting - Category: '{}', Title: '{}', Name: '{}', ItemId: {}",
                           orderItem.getServiceCategory(), orderItem.getServiceTitle(), 
                           orderItem.getServiceName(), orderItem.getServiceItemId());
            } else {
                // Fallback for backward compatibility
                orderItem.setServiceName(serviceRequest.getName());
                orderItem.setServiceCategory(serviceRequest.getCategoryName());
                orderItem.setServiceTitle(serviceRequest.getServiceTitle());
                logger.warn("Using provided service data (no service item found): {}", serviceRequest.getServiceId());
                
                // *** DEBUG: Log fallback data ***
                logger.info("🔍 DEBUG: Using fallback data - Category: '{}', Title: '{}', Name: '{}'",
                           orderItem.getServiceCategory(), orderItem.getServiceTitle(), orderItem.getServiceName());
            }
            
            // *** CRITICAL FIX: Save the order item and add to both database and collection ***
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            createdOrderItems.add(savedOrderItem);
            
            // *** CRITICAL FIX: Add the saved item to the order's collection ***
            savedOrder.getOrderItems().add(savedOrderItem);
            
            calculatedTotal = calculatedTotal.add(savedOrderItem.getTotalPrice());
            
            logger.info("✅ OrderItem created and added to collection: {} with category '{}' and title '{}'", 
                       savedOrderItem.getId(), savedOrderItem.getServiceCategory(), savedOrderItem.getServiceTitle());
        }
        
        // Set the first service as the main service for compatibility
        if (!request.getServices().isEmpty()) {
            RepairService firstService = findOrCreateRepairService(request.getServices().get(0).getServiceId());
            savedOrder.setService(firstService);
        }
        
        // Update order with final data
        savedOrder = orderRepository.save(savedOrder);
        
        // Create invoice for the order
        createInvoiceForOrder(savedOrder);
        
        // *** VERIFY COLLECTION STATE BEFORE RETURNING ***
        logger.info("✅ FINAL: Order items collection size: {} for order {}", 
                   savedOrder.getOrderItems().size(), savedOrder.getOrderNumber());
                   
        // *** DEBUG: Log each order item's field values in final collection ***
        for (int i = 0; i < savedOrder.getOrderItems().size(); i++) {
            OrderItem item = savedOrder.getOrderItems().get(i);
            logger.info("🔍 FINAL: OrderItem[{}] - Category: '{}', Title: '{}', Name: '{}', ItemId: {}",
                       i, item.getServiceCategory(), item.getServiceTitle(), 
                       item.getServiceName(), item.getServiceItemId());
        }
        
        logger.info("✅ ENHANCED: Order created with proper category information: {} for user: {} with {} services", 
                   orderNumber, currentUser.getPhone(), request.getServices().size());
        return savedOrder;
    }

    /**
     * *** ORIGINAL METHOD - keeping for reference ***
     */
    private RepairOrder createOrderWithMultipleServicesOriginal(CreateOrderRequest request) {
        User currentUser = authService.getCurrentUser();
        
        // Validate address
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
        
        // Verify address belongs to current user
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Address does not belong to current user");
        }
        
        // Generate unique order number
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        
        // Create order
        RepairOrder order = new RepairOrder();
        order.setOrderNumber(orderNumber);
        order.setUser(currentUser);
        order.setTotalPrice(request.getTotalAmount());
        order.setProblemDescription(request.getProblemDescription());
        order.setPickupAddress(address.getFullAddress());
        order.setCustomerNotes(request.getCustomerNotes());
        order.setStatus(RepairOrder.OrderStatus.PENDING);
        order.setAddress(address);
        
        // Save order first to get ID
        RepairOrder savedOrder = orderRepository.save(order);
        
        // Create order items for each service
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderServiceRequest serviceRequest : request.getServices()) {
            RepairService service = findOrCreateRepairService(serviceRequest.getServiceId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setService(service);
            orderItem.setQuantity(serviceRequest.getQuantity());
            orderItem.setUnitPrice(serviceRequest.getPrice());
            orderItem.setTotalPrice(serviceRequest.getPrice().multiply(BigDecimal.valueOf(serviceRequest.getQuantity())));
            orderItem.setNotes(serviceRequest.getNotes());
            
            orderItemRepository.save(orderItem);
            calculatedTotal = calculatedTotal.add(orderItem.getTotalPrice());
        }
        
        // Set the first service as the main service for compatibility
        if (!request.getServices().isEmpty()) {
            RepairService firstService = findOrCreateRepairService(request.getServices().get(0).getServiceId());
            savedOrder.setService(firstService);
        }
        
        // Verify total amount matches calculated total
        if (calculatedTotal.compareTo(request.getTotalAmount()) != 0) {
            logger.warn("Total amount mismatch: provided={}, calculated={}", request.getTotalAmount(), calculatedTotal);
        }
        
        // Update order with final data
        savedOrder = orderRepository.save(savedOrder);
        
        // Create invoice for the order
        createInvoiceForOrder(savedOrder);
        
        logger.info("New multi-service order created: {} for user: {} with {} services", 
                   orderNumber, currentUser.getPhone(), request.getServices().size());
        return savedOrder;
    }

    /**
     * Create new repair order
     */
    public RepairOrder createOrder(Long serviceId, String problemDescription, 
                                 String pickupAddress, String customerNotes) {
        User currentUser = authService.getCurrentUser();
        
        RepairService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairService", "id", serviceId));

        // Generate unique order number
        String orderNumber = OrderNumberGenerator.generateOrderNumber();

        RepairOrder order = new RepairOrder();
        order.setOrderNumber(orderNumber);
        order.setUser(currentUser);
        order.setService(service);
        order.setTotalPrice(service.getBasePrice());
        order.setProblemDescription(problemDescription);
        order.setPickupAddress(pickupAddress);
        order.setCustomerNotes(customerNotes);
        order.setStatus(RepairOrder.OrderStatus.PENDING);

        RepairOrder savedOrder = orderRepository.save(order);

        // Create invoice for the order
        createInvoiceForOrder(savedOrder);

        logger.info("New order created: {} for Mobile: {}", orderNumber, currentUser.getPhone());
        return savedOrder;
    }

    /**
     * Get user orders
     */
    @Transactional(readOnly = true)
    public List<RepairOrder> getUserOrders(Long userId) {
        // Use a proper repository method that queries by user ID directly
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * *** ENHANCED: Get user orders with actual cart item details ***
     * Now uses proper JOIN FETCH queries to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public List<RepairOrder> getUserOrdersWithItems(Long userId) {
        try {
            logger.info("Fetching orders for user ID: {}", userId);
            
            // Use enhanced repository query with JOIN FETCH for orderItems
            List<RepairOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            logger.info("Found {} orders for user ID: {}", orders.size(), userId);
            
            // Log details about order items for debugging
            for (RepairOrder order : orders) {
                logger.info("Order {} has {} order items", order.getOrderNumber(), order.getOrderItems().size());
                
                // Debug log the new order item fields
                for (OrderItem item : order.getOrderItems()) {
                    logger.info("OrderItem - Category: '{}', Title: '{}', Name: '{}', ServiceItemId: {}", 
                               item.getServiceCategory(), item.getServiceTitle(), 
                               item.getServiceName(), item.getServiceItemId());
                }
            }
            
            return orders;
            
        } catch (Exception e) {
            logger.error("Error fetching orders with items for user ID: {}", userId, e);
            
            // Fallback: try simple query without JOIN FETCH
            logger.warn("Attempting fallback query without JOIN FETCH");
            try {
                List<RepairOrder> fallbackOrders = orderRepository.findAllById(
                    orderRepository.findAll().stream()
                        .filter(o -> o.getUser().getId().equals(userId))
                        .map(RepairOrder::getId)
                        .toList()
                );
                
                // Try to initialize order items manually
                fallbackOrders.forEach(order -> {
                    try {
                        order.getOrderItems().size(); // Force lazy loading
                    } catch (Exception ie) {
                        logger.warn("Failed to load order items for order {}: {}", order.getOrderNumber(), ie.getMessage());
                        // Set empty list to avoid serialization issues
                        order.setOrderItems(new ArrayList<>());
                    }
                });
                
                return fallbackOrders;
                
            } catch (Exception fallbackException) {
                logger.error("Fallback query also failed for user ID: {}", userId, fallbackException);
                throw new RuntimeException("Unable to fetch user orders: " + fallbackException.getMessage(), fallbackException);
            }
        }
    }

    /**
     * Get user orders with pagination
     */
    public Page<RepairOrder> getUserOrdersWithPagination(Long userId, Pageable pageable) {
        // Use a proper repository method that queries by user ID directly
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get order by ID
     */
    public RepairOrder getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "id", orderId));
    }

    /**
     * Get order by order number
     */
    public RepairOrder getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "orderNumber", orderNumber));
    }
    
    /**
     * *** ENHANCED: Get order by order number with complete details including orderItems ***
     * This method returns the same detailed structure as order creation response
     */
    @Transactional(readOnly = true)
    public RepairOrder getOrderDetailsByOrderNumber(String orderNumber) {
        try {
            logger.info("Fetching detailed order information for order number: {}", orderNumber);
            
            // Use repository method with JOIN FETCH to get complete order data
            RepairOrder order = orderRepository.findByOrderNumberWithAssociations(orderNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "orderNumber", orderNumber));
            
            logger.info("Found order {} with {} order items", order.getOrderNumber(), order.getOrderItems().size());
            
            // Debug log the order items for troubleshooting
            for (OrderItem item : order.getOrderItems()) {
                logger.info("OrderItem details - ID: {}, Category: '{}', Title: '{}', Name: '{}', ServiceItemId: {}, Price: {}", 
                           item.getId(), item.getServiceCategory(), item.getServiceTitle(), 
                           item.getServiceName(), item.getServiceItemId(), item.getUnitPrice());
            }
            
            return order;
            
        } catch (ResourceNotFoundException e) {
            logger.error("Order not found with number: {}", orderNumber);
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching order details for order number: {}", orderNumber, e);
            
            // Fallback: try basic query and manually load associations
            logger.warn("Attempting fallback query for order: {}", orderNumber);
            try {
                RepairOrder fallbackOrder = orderRepository.findByOrderNumber(orderNumber)
                        .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "orderNumber", orderNumber));
                
                // Force loading of associations
                fallbackOrder.getOrderItems().size(); // Trigger lazy loading
                if (fallbackOrder.getService() != null) {
                    fallbackOrder.getService().getName(); // Ensure service is loaded
                }
                if (fallbackOrder.getAddress() != null) {
                    fallbackOrder.getAddress().getFullAddress(); // Ensure address is loaded
                }
                
                logger.info("Fallback successful for order: {}", orderNumber);
                return fallbackOrder;
                
            } catch (Exception fallbackException) {
                logger.error("Fallback query also failed for order: {}", orderNumber, fallbackException);
                throw new RuntimeException("Unable to fetch order details: " + fallbackException.getMessage(), fallbackException);
            }
        }
    }

    /**
     * Update order status (for technicians/admin)
     */
    public RepairOrder updateOrderStatus(Long orderId, RepairOrder.OrderStatus status, String technicianNotes) {
        RepairOrder order = getOrderById(orderId);
        
        order.setStatus(status);
        if (technicianNotes != null) {
            order.setTechnicianNotes(technicianNotes);
        }

        // Set completion date if order is completed
        if (status == RepairOrder.OrderStatus.COMPLETED && order.getCompletedDate() == null) {
            order.setCompletedDate(LocalDateTime.now());
        }

        RepairOrder updatedOrder = orderRepository.save(order);
        logger.info("Order status updated: {} to {}", order.getOrderNumber(), status);
        
        return updatedOrder;
    }

    /**
     * Assign technician to order
     */
    public RepairOrder assignTechnician(Long orderId, Long technicianId) {
        RepairOrder order = getOrderById(orderId);
        
        // Get the actual User entity from database to avoid detached entity issues
        User technician = orderRepository.findUserById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", technicianId));
        
        order.setTechnician(technician);
        order.setStatus(RepairOrder.OrderStatus.CONFIRMED);

        RepairOrder updatedOrder = orderRepository.save(order);
        logger.info("Technician assigned to order: {}", order.getOrderNumber());
        
        return updatedOrder;
    }

    /**
     * Schedule order
     */
    public RepairOrder scheduleOrder(Long orderId, LocalDateTime scheduledDate) {
        RepairOrder order = getOrderById(orderId);
        order.setScheduledDate(scheduledDate);
        
        RepairOrder updatedOrder = orderRepository.save(order);
        logger.info("Order scheduled: {} for {}", order.getOrderNumber(), scheduledDate);
        
        return updatedOrder;
    }

    /**
     * Cancel order
     */
    @Transactional
    public RepairOrder cancelOrder(Long orderId, String reason) {
        // Use eager fetching to prevent lazy loading issues during serialization
        RepairOrder order = orderRepository.findByIdWithAssociations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "id", orderId));
        
        if (order.getStatus() == RepairOrder.OrderStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed order");
        }
        
        if (order.getStatus() == RepairOrder.OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        order.setStatus(RepairOrder.OrderStatus.CANCELLED);
        order.setTechnicianNotes("Cancelled: " + reason);

        RepairOrder cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled: {}", order.getOrderNumber());
        
        return cancelledOrder;
    }

    /**
     * Cancel order by order number (Frontend compatible)
     */
    @Transactional
    public RepairOrder cancelOrderByNumber(String orderNumber, String reason) {
        // Use eager fetching to prevent lazy loading issues during serialization
        RepairOrder order = orderRepository.findByOrderNumberWithAssociations(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("RepairOrder", "orderNumber", orderNumber));
        
        // Verify order belongs to current user
        User currentUser = authService.getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Order does not belong to current user");
        }
        
        if (order.getStatus() != RepairOrder.OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled");
        }

        order.setStatus(RepairOrder.OrderStatus.CANCELLED);
        order.setTechnicianNotes("Cancelled by customer: " + reason);

        RepairOrder cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled by customer: {}", order.getOrderNumber());
        
        return cancelledOrder;
    }

    /**
     * Get order items for a specific order
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        RepairOrder order = getOrderById(orderId);
        return orderItemRepository.findByOrderId(orderId);
    }

    /**
     * Get orders by status
     */
    public List<RepairOrder> getOrdersByStatus(RepairOrder.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get recent orders (last 30 days)
     */
    public List<RepairOrder> getRecentOrders() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findRecentOrders(thirtyDaysAgo);
    }

    /**
     * Search orders
     */
    public List<RepairOrder> searchOrders(String searchTerm) {
        return orderRepository.searchOrders(searchTerm);
    }

    /**
     * Get unassigned orders
     */
    public List<RepairOrder> getUnassignedOrders() {
        return orderRepository.findUnassignedOrders();
    }

    /**
     * Create invoice for order
     */
    private void createInvoiceForOrder(RepairOrder order) {
        String invoiceNumber = OrderNumberGenerator.generateInvoiceNumber();
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setOrder(order);
        invoice.setAmount(order.getTotalPrice());
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setDueDate(LocalDateTime.now().plusDays(30));

        invoiceRepository.save(invoice);
        logger.info("Invoice created: {} for order: {}", invoiceNumber, order.getOrderNumber());
    }
    
    /**
     * Find existing RepairService or create one from ServiceItem
     * This bridges the gap between the new ServiceItem system and legacy RepairService system
     */
    private RepairService findOrCreateRepairService(Long serviceItemId) {
        // First, try to find an existing RepairService with this ID (in case it's actually a RepairService ID)
        RepairService existingService = serviceRepository.findById(serviceItemId).orElse(null);
        if (existingService != null) {
            logger.debug("Found existing RepairService with ID: {}", serviceItemId);
            return existingService;
        }
        
        // If not found, this is likely a ServiceItem ID, so get the ServiceItem and create a RepairService
        com.digicashon.digicashonbackendservice.entity.ServiceItem serviceItem = 
            serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceItem", "id", serviceItemId));
        
        logger.info("Creating RepairService from ServiceItem ID: {} - {}", serviceItemId, serviceItem.getServiceTitle());
        
        // Create a new RepairService based on the ServiceItem
        RepairService repairService = new RepairService();
        repairService.setName(serviceItem.getServiceTitle());
        repairService.setDescription(serviceItem.getServiceDescription());
        repairService.setBasePrice(BigDecimal.valueOf(serviceItem.getPrice()));
        repairService.setEstimatedDurationHours(2); // Default duration
        repairService.setIsActive(serviceItem.getIsActive());
        repairService.setIsFeatured(false); // Default
        
        // Map device type based on service category or default to LAPTOP
        RepairService.DeviceType deviceType = RepairService.DeviceType.LAPTOP; // Default
        if (serviceItem.getServiceCategory() != null) {
            String categoryName = serviceItem.getServiceCategory().getCategoryName().toLowerCase();
            if (categoryName.contains("mobile") || categoryName.contains("phone")) {
                deviceType = RepairService.DeviceType.MOBILE;
            } else if (categoryName.contains("tablet")) {
                deviceType = RepairService.DeviceType.TABLET;
            } else if (categoryName.contains("watch")) {
                deviceType = RepairService.DeviceType.WATCH;
            }
        }
        repairService.setDeviceType(deviceType);
        
        // Save and return the new RepairService
        RepairService savedService = serviceRepository.save(repairService);
        logger.info("Created RepairService with ID: {} for ServiceItem: {}", savedService.getId(), serviceItemId);
        
        return savedService;
    }
    
    // *** NEW: Helper class for cart item details ***
    public static class CartItemDetails {
        private String serviceName;
        private String categoryName;
        
        public CartItemDetails(String serviceName, String categoryName) {
            this.serviceName = serviceName;
            this.categoryName = categoryName;
        }
        
        public String getServiceName() { return serviceName; }
        public String getCategoryName() { return categoryName; }
    }
}
