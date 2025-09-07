package com.digicashon.digicashonbackendservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceCategoryGroupDto {

    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private String categoryIcon;
    private Integer displayOrder;
    private Integer serviceCount;
    private String bulletPoint1;
    private String bulletPoint2;
    private List<ServiceItemDto> services;

    public ServiceCategoryGroupDto(Long categoryId, String categoryName, String categoryDescription, 
                                  String categoryIcon, Integer displayOrder,String bulletPoint1, String bulletPoint2, List<ServiceItemDto> services) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.categoryIcon = categoryIcon;
        this.displayOrder = displayOrder;
        this.bulletPoint1 = bulletPoint1;
        this.bulletPoint2 = bulletPoint2;
        this.services = services;
        this.serviceCount = services != null ? services.size() : 0;

    }
}
