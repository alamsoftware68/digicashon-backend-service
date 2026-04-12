package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "device_graphics")
@Immutable
public class AdminDeviceGraphics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "graphics_name")
    private String graphicsName;

    @Column(name = "price_impact", precision = 10, scale = 2)
    private BigDecimal priceImpact;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    public Long getId() { return id; }
    public String getGraphicsName() { return graphicsName; }
    public BigDecimal getPriceImpact() { return priceImpact; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Boolean getEnabled() { return enabled; }
}
