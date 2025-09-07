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
public class ServiceCategoryResponseDto {

    private Integer totalCategories;
    private Integer totalServices;
    private List<String> availableCategories;
    private List<ServiceCategoryGroupDto> categoryGroups;

    public ServiceCategoryResponseDto(List<ServiceCategoryGroupDto> categoryGroups) {
        this.categoryGroups = categoryGroups;
        this.totalCategories = categoryGroups != null ? categoryGroups.size() : 0;
        this.totalServices = categoryGroups != null ? 
            categoryGroups.stream().mapToInt(group -> group.getServiceCount()).sum() : 0;
        this.availableCategories = categoryGroups != null ? 
            categoryGroups.stream().map(ServiceCategoryGroupDto::getCategoryName).toList() : null;
    }
}
