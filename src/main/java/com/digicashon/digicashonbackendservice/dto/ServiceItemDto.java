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
public class ServiceItemDto {

    private Long id;
    private String serviceTitle;
    private String serviceDescription;
    private List<String> bulletPoints;
    private String serviceIcon;
    private Double price;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert a ServiceItem entity to ServiceItemDto
     * @param entity The ServiceItem entity to convert
     * @return The converted ServiceItemDto
     */
    public static ServiceItemDto fromEntity(com.digicashon.digicashonbackendservice.entity.ServiceItem entity) {
        if (entity == null) {
            return null;
        }
        
        ServiceItemDto dto = new ServiceItemDto();
        dto.setId(entity.getId());
        dto.setServiceTitle(entity.getServiceTitle());
        dto.setServiceDescription(entity.getServiceDescription());
        dto.setBulletPoints(List.of(entity.getBulletPoint1(), entity.getBulletPoint2(), 
                            entity.getBulletPoint3(), entity.getBulletPoint4()));
        dto.setServiceIcon(entity.getServiceIcon());
        dto.setPrice(entity.getPrice());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    // Constructor for mapping from entity
    public ServiceItemDto(Long id, String serviceTitle, String serviceDescription,
                         String bulletPoint1, String bulletPoint2, String bulletPoint3, String bulletPoint4,
                         String serviceIcon, Double price, Integer displayOrder, Boolean isActive,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.serviceTitle = serviceTitle;
        this.serviceDescription = serviceDescription;
        this.bulletPoints = List.of(bulletPoint1, bulletPoint2, bulletPoint3, bulletPoint4);
        this.serviceIcon = serviceIcon;
        this.price = price;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
