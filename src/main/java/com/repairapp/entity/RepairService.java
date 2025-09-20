package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Repair Service entity representing available repair services
 */
@Entity
@Table(name = "repair_services")
@JsonIgnoreProperties({"features", "orders", "orderItems", "category"}) // Prevent lazy loading issues
public class RepairService extends BaseEntity {

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 digits and 2 decimal places")
    @Column(name = "base_price", nullable = false, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    @Column(name = "estimated_duration_hours", nullable = false)
    private Integer estimatedDurationHours;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "badge", length = 50)
    private String badge; // Popular, Premium, Quick Fix, etc.

    @ElementCollection
    @CollectionTable(name = "service_features", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "feature")
    @JsonIgnore // Prevent lazy loading issues in JSON serialization
    private List<String> features = new ArrayList<>();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore // Prevent lazy loading issues
    private ServiceCategory category;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @JsonIgnore // Prevent lazy loading issues
    private List<RepairOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @JsonIgnore // Prevent lazy loading issues
    private List<OrderItem> orderItems = new ArrayList<>();

    // Constructors
    public RepairService() {}

    public RepairService(String name, String description, DeviceType deviceType, 
                        BigDecimal basePrice, Integer estimatedDurationHours) {
        this.name = name;
        this.description = description;
        this.deviceType = deviceType;
        this.basePrice = basePrice;
        this.estimatedDurationHours = estimatedDurationHours;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public List<RepairOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<RepairOrder> orders) {
        this.orders = orders;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // Device types enum
    public enum DeviceType {
        LAPTOP("Laptop"),
        MOBILE("Mobile Phone"),
        TABLET("Tablet"),
        WATCH("Smart Watch");

        private final String displayName;

        DeviceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
