package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/**
 * Read-only entity for super categories from Admin database
 */
@Entity
@Table(name = "super_categories")
@Immutable
public class AdminSuperCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "bullet_1")
    private String bullet1;

    @Column(name = "bullet_2")
    private String bullet2;

    @Column(name = "bullet_3")
    private String bullet3;

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

    public String getBullet1() {
        return bullet1;
    }

    public String getBullet2() {
        return bullet2;
    }

    public String getBullet3() {
        return bullet3;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
