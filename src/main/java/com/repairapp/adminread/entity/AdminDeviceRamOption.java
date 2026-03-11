package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device RAM options from Admin database
 */
@Entity
@Table(name = "device_ram_options")
@Immutable
public class AdminDeviceRamOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity_label")
    private String capacityLabel;

    @Column(name = "capacity_mb")
    private Integer capacityMb;

    @Column(name = "ram_type")
    private String ramType;

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

    public String getCapacityLabel() {
        return capacityLabel;
    }

    public Integer getCapacityMb() {
        return capacityMb;
    }

    public String getRamType() {
        return ramType;
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
