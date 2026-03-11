package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device screen sizes from Admin database
 */
@Entity
@Table(name = "device_screen_sizes")
@Immutable
public class AdminDeviceScreenSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_label")
    private String sizeLabel;

    @Column(name = "min_inches", precision = 5, scale = 2)
    private BigDecimal minInches;

    @Column(name = "max_inches", precision = 5, scale = 2)
    private BigDecimal maxInches;

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

    public String getSizeLabel() {
        return sizeLabel;
    }

    public BigDecimal getMinInches() {
        return minInches;
    }

    public BigDecimal getMaxInches() {
        return maxInches;
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
