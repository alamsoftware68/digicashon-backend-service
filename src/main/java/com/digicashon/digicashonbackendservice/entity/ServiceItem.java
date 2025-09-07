package com.digicashon.digicashonbackendservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @Column(name = "service_title", nullable = false, length = 150)
    private String serviceTitle;

    @Column(name = "service_description", nullable = false, length = 500)
    private String serviceDescription;

    @Column(name = "bullet_point_1", nullable = false, length = 100)
    private String bulletPoint1;

    @Column(name = "bullet_point_2", nullable = false, length = 100)
    private String bulletPoint2;

    @Column(name = "bullet_point_3", nullable = false, length = 100)
    private String bulletPoint3;

    @Column(name = "bullet_point_4", nullable = false, length = 100)
    private String bulletPoint4;

    @Column(name = "service_icon", length = 255)
    private String serviceIcon;

    @Column(name = "price")
    private Double price;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Constructor for easy data insertion
    public ServiceItem(ServiceCategory serviceCategory, String serviceTitle, String serviceDescription,
                      String bulletPoint1, String bulletPoint2, String bulletPoint3, 
                      String bulletPoint4, Integer displayOrder) {
        this.serviceCategory = serviceCategory;
        this.serviceTitle = serviceTitle;
        this.serviceDescription = serviceDescription;
        this.bulletPoint1 = bulletPoint1;
        this.bulletPoint2 = bulletPoint2;
        this.bulletPoint3 = bulletPoint3;
        this.bulletPoint4 = bulletPoint4;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdBy = "SYSTEM";
        this.updatedBy = "SYSTEM";
    }
}
