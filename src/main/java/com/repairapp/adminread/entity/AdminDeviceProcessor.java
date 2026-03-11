package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device processors from Admin database
 */
@Entity
@Table(name = "device_processors")
@Immutable
public class AdminDeviceProcessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processor_name")
    private String processorName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "generation")
    private String generation;

    @Column(name = "core_count")
    private Integer coreCount;

    @Column(name = "base_clock_speed")
    private BigDecimal baseClockSpeed;

    @Column(name = "price_impact")
    private BigDecimal priceImpact;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getProcessorName() {
        return processorName;
    }

    public String getBrand() {
        return brand;
    }

    public String getGeneration() {
        return generation;
    }

    public Integer getCoreCount() {
        return coreCount;
    }

    public BigDecimal getBaseClockSpeed() {
        return baseClockSpeed;
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
