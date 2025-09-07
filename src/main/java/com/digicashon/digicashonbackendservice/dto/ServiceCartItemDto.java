package com.digicashon.digicashonbackendservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Service Cart Item data transfer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceCartItemDto {
    
    private Long id;
    private Long cartId;
    private Long serviceItemId;
    
    // Service details (copied for cart display)
    private String serviceTitle;
    private String serviceDescription;
    private String categoryName;
    private String serviceIcon;
    
    // Cart item details
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional service information (from ServiceItem entity)
    private ServiceItemDto serviceDetails;
    
    // Cart item metadata
    private Long version;
    
    public static ServiceCartItemDto fromEntity(com.digicashon.digicashonbackendservice.entity.ServiceCartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        ServiceCartItemDtoBuilder builder = ServiceCartItemDto.builder()
            .id(cartItem.getId())
            .serviceItemId(cartItem.getServiceItem() != null ? cartItem.getServiceItem().getId() : null)
            .serviceTitle(cartItem.getServiceTitle())
            .serviceDescription(cartItem.getServiceDescription())
            .categoryName(cartItem.getCategoryName())
            .quantity(cartItem.getQuantity())
            .unitPrice(cartItem.getUnitPrice())
            .totalPrice(cartItem.getTotalPrice())
            .notes(cartItem.getNotes())
            .createdAt(cartItem.getCreatedAt())
            .updatedAt(cartItem.getUpdatedAt())
            .version(cartItem.getVersion());
        
        // Add cart ID if available
        if (cartItem.getServiceCart() != null) {
            builder.cartId(cartItem.getServiceCart().getId());
        }
        
        // Add service icon from service item if available
        if (cartItem.getServiceItem() != null) {
            builder.serviceIcon(cartItem.getServiceItem().getServiceIcon());
            
            // Optionally include full service details
            builder.serviceDetails(ServiceItemDto.fromEntity(cartItem.getServiceItem()));
        }
        
        return builder.build();
    }
    
    /**
     * Create a simple cart item DTO for frontend (matches frontend cart structure)
     */
    public static ServiceCartItemDto forFrontend(com.digicashon.digicashonbackendservice.entity.ServiceCartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        return ServiceCartItemDto.builder()
            .id(cartItem.getId())
            .serviceItemId(cartItem.getServiceItem() != null ? cartItem.getServiceItem().getId() : null)
            .serviceTitle(cartItem.getServiceTitle()) // Maps to frontend "name"
            .unitPrice(cartItem.getUnitPrice()) // Maps to frontend "price"
            .totalPrice(cartItem.getTotalPrice())
            .quantity(cartItem.getQuantity())
            .categoryName(cartItem.getCategoryName())
            .serviceDescription(cartItem.getServiceDescription())
            .serviceIcon(cartItem.getServiceItem() != null ? cartItem.getServiceItem().getServiceIcon() : null)
            .notes(cartItem.getNotes())
            .createdAt(cartItem.getCreatedAt())
            .build();
    }
    
    /**
     * Convert to frontend-compatible format (name/price structure)
     */
    public FrontendCartItemDto toFrontendFormat() {
        return FrontendCartItemDto.builder()
            .id(this.id)
            .name(this.serviceTitle)
            .price(this.unitPrice)
            .quantity(this.quantity)
            .totalPrice(this.totalPrice)
            .category(this.categoryName)
            .description(this.serviceDescription)
            .icon(this.serviceIcon)
            .notes(this.notes)
            .serviceItemId(this.serviceItemId)
            .build();
    }
}

/**
 * DTO that matches frontend cart item structure exactly
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class FrontendCartItemDto {
    private Long id;
    private Long serviceItemId;
    private String name; // Maps to serviceTitle
    private Double price; // Maps to unitPrice
    private Integer quantity;
    private Double totalPrice;
    private String category;
    private String description;
    private String icon;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedAt;
}
