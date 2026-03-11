package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for brand models from Admin database
 */
@Entity
@Table(name = "brand_models")
@Immutable
public class AdminBrandModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "brand_type")
    private String brandType;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getBrandId() {
        return brandId;
    }

    public String getBrandType() {
        return brandType;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
