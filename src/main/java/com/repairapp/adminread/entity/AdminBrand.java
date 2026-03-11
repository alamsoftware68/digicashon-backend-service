package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/**
 * Read-only entity for brands from Admin database
 */
@Entity
@Table(name = "brands")
@Immutable
public class AdminBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "brand_type")
    private String brandType;

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

    public String getBrandType() {
        return brandType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
