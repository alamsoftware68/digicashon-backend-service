package com.digicashon.digicashonbackendservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceCategoryDto {

    private Long id;
    private String categoryName;
    private String categoryDescription;
    private String categoryIcon;
    private Integer displayOrder;
    private Integer serviceCount;
    private List<ServiceItemDto> services;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for basic category info
    public ServiceCategoryDto(Long id, String categoryName, String categoryDescription,
                             String categoryIcon, Integer displayOrder, Boolean isActive,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.categoryIcon = categoryIcon;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
