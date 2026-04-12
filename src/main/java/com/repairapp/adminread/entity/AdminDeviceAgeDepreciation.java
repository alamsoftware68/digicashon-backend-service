package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device age depreciation from Admin database
 */
@Entity
@Table(name = "device_age_depreciations")
@Immutable
public class AdminDeviceAgeDepreciation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "age_code")
    private String ageCode;

    @Column(name = "age_label")
    private String ageLabel;

    @Column(name = "depreciation_percentage", precision = 5, scale = 2)
    private BigDecimal depreciationPercentage;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getAgeCode() {
        return ageCode;
    }

    public String getAgeLabel() {
        return ageLabel;
    }

    public BigDecimal getDepreciationPercentage() {
        return depreciationPercentage;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
