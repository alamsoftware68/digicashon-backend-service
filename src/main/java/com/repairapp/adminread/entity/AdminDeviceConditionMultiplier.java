package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device condition multipliers from Admin database
 */
@Entity
@Table(name = "device_condition_multipliers")
@Immutable
public class AdminDeviceConditionMultiplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "condition_code")
    private String conditionCode;

    @Column(name = "condition_label")
    private String conditionLabel;

    @Column(name = "multiplier_percentage", precision = 5, scale = 2)
    private BigDecimal multiplierPercentage;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public String getConditionLabel() {
        return conditionLabel;
    }

    public BigDecimal getMultiplierPercentage() {
        return multiplierPercentage;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
