package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device storage options from Admin database
 */
@Entity
@Table(name = "device_storage_options")
@Immutable
public class AdminDeviceStorageOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_label")
    private String storageLabel;

    @Column(name = "storage_type")
    private String storageType;

    @Column(name = "capacity_gb")
    private Integer capacityGb;

    @Column(name = "price_impact", precision = 10, scale = 2)
    private BigDecimal priceImpact;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getStorageLabel() {
        return storageLabel;
    }

    public String getStorageType() {
        return storageType;
    }

    public Integer getCapacityGb() {
        return capacityGb;
    }

    public BigDecimal getPriceImpact() {
        return priceImpact;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
