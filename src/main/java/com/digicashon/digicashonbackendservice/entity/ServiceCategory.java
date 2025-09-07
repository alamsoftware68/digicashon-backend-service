package com.digicashon.digicashonbackendservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "LaptopServiceCategory")
@Table(name = "service_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100, unique = true)
    private String categoryName;

    @Column(name = "category_description", length = 500)
    private String categoryDescription;

    @Column(name = "category_icon", length = 255)
    private String categoryIcon;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "serviceCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ServiceItem> serviceItems = new ArrayList<>();

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

    @Column(name = "bullet_point_1", nullable = false, length = 100)
    private String bulletPoint1;

    @Column(name = "bullet_point_2", nullable = false, length = 100)
    private String bulletPoint2;

    // Constructor for easy data insertion
    public ServiceCategory(String categoryName, String categoryDescription, String categoryIcon, Integer displayOrder, String bulletPoint1, String bulletPoint2) {
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.categoryIcon = categoryIcon;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdBy = "SYSTEM";
        this.updatedBy = "SYSTEM";
        this.bulletPoint1 = bulletPoint1;
        this.bulletPoint2 = bulletPoint2;
    }
}
