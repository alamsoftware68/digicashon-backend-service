package com.repairapp.dto.order;

import com.repairapp.entity.RepairOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for admin portal consumption of repair orders.
 * Maps from RepairOrder entity within a transaction so lazy fields are accessible.
 */
public class RepairOrderAdminDto {

    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalPrice;
    private String problemDescription;
    private String pickupAddress;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledDate;
    private String serviceName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    public static RepairOrderAdminDto fromEntity(RepairOrder order) {
        RepairOrderAdminDto dto = new RepairOrderAdminDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalPrice(order.getTotalPrice());
        dto.setProblemDescription(order.getProblemDescription());
        dto.setPickupAddress(order.getPickupAddress());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setScheduledDate(order.getScheduledDate());

        // Get service name from orderItems (EAGER loaded — safe to access)
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            String sn = order.getOrderItems().get(0).getServiceName();
            if (sn == null && order.getOrderItems().get(0).getService() != null) {
                sn = order.getOrderItems().get(0).getService().getName();
            }
            dto.setServiceName(sn);
        }

        // Fallback: get service name from the main service field (lazy — accessed within transaction)
        if (dto.getServiceName() == null && order.getService() != null) {
            dto.setServiceName(order.getService().getName());
        }

        // Customer info from lazy user field — accessible within transaction
        if (order.getUser() != null) {
            dto.setCustomerName(order.getUser().getName());
            dto.setCustomerPhone(order.getUser().getPhone());
            dto.setCustomerEmail(order.getUser().getEmail());
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
