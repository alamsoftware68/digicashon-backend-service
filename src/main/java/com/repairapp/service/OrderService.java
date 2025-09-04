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

    /**
     * Create new repair order with multiple services (Frontend compatible)
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
        
        // Create order items for each service
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderServiceRequest serviceRequest : request.getServices()) {
            RepairService service = serviceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("RepairService", "id", serviceRequest.getServiceId()));
            
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
            RepairService firstService = serviceRepository.findById(request.getServices().get(0).getServiceId())
                    .orElse(null);
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
    public List<RepairOrder> getUserOrders(Long userId) {
        User user = new User();
        user.setId(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get user orders with pagination
     */
    public Page<RepairOrder> getUserOrdersWithPagination(Long userId, Pageable pageable) {
        User user = new User();
        user.setId(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
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
        
        User technician = new User();
        technician.setId(technicianId);
        
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
    public RepairOrder cancelOrder(Long orderId, String reason) {
        RepairOrder order = getOrderById(orderId);
        
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
    public RepairOrder cancelOrderByNumber(String orderNumber, String reason) {
        RepairOrder order = getOrderByOrderNumber(orderNumber);
        
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
}
