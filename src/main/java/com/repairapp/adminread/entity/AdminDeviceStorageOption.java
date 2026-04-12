package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "device_storage_options")
@Immutable
public class AdminDeviceStorageOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_label")
    private String storageLabel;

    @Column(name = "price_impact", precision = 10, scale = 2)
    private BigDecimal priceImpact;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    public Long getId() { return id; }
    public String getStorageLabel() { return storageLabel; }
    public BigDecimal getPriceImpact() { return priceImpact; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Boolean getEnabled() { return enabled; }
}
