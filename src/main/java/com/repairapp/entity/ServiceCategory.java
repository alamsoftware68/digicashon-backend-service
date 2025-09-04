package com.repairapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Category entity for organizing repair services
 */
@Entity
@Table(name = "service_categories")
public class ServiceCategory extends BaseEntity {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 10, message = "Icon must not exceed 10 characters")
    @Column(name = "icon", length = 10)
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private RepairService.DeviceType deviceType;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairService> services = new ArrayList<>();

    // Constructors
    public ServiceCategory() {}

    public ServiceCategory(String name, String description, String icon, RepairService.DeviceType deviceType) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.deviceType = deviceType;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public RepairService.DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(RepairService.DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public List<RepairService> getServices() {
        return services;
    }

    public void setServices(List<RepairService> services) {
        this.services = services;
    }
}
