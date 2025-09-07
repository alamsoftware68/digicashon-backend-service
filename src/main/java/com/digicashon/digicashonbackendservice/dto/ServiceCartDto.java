package com.digicashon.digicashonbackendservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Service Cart data transfer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceCartDto {
    
    private Long id;
    private String sessionId;
    private Long userId;
    private String userName;
    private Double totalAmount;
    private Integer itemCount;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<ServiceCartItemDto> items;
    
    // Summary information
    private Boolean isEmpty;
    private Boolean isExpired;
    private Integer uniqueServicesCount;
    private Integer totalQuantity;
    
    // Cart metadata
    private Long version;
    
    public static ServiceCartDto fromEntity(com.digicashon.digicashonbackendservice.entity.ServiceCart cart) {
        if (cart == null) {
            return null;
        }
        
        ServiceCartDtoBuilder builder = ServiceCartDto.builder()
            .id(cart.getId())
            .sessionId(cart.getSessionId())
            .totalAmount(cart.getTotalAmount())
            .itemCount(cart.getItemCount())
            .isActive(cart.getIsActive())
            .expiresAt(cart.getExpiresAt())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .isEmpty(cart.isEmpty())
            .isExpired(cart.isExpired())
            .version(cart.getVersion());
        
        // Add user information if available
        if (cart.getUser() != null) {
            builder.userId(cart.getUser().getId())
                   .userName(cart.getUser().getName());
        }
        
        // Add cart items if loaded
        if (cart.getServiceCartItems() != null && !cart.getServiceCartItems().isEmpty()) {
            List<ServiceCartItemDto> itemDtos = cart.getServiceCartItems().stream()
                .map(ServiceCartItemDto::fromEntity)
                .toList();
            
            builder.items(itemDtos);
            builder.uniqueServicesCount(itemDtos.size());
            builder.totalQuantity(itemDtos.stream()
                .mapToInt(ServiceCartItemDto::getQuantity)
                .sum());
        }
        
        return builder.build();
    }
}

/**
 * DTO for Add to Cart requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class AddToServiceCartRequest {
    private Long serviceItemId;
    private Integer quantity;
    private String notes;
    private Boolean replaceIfExists;
}

/**
 * DTO for Remove from Cart requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class RemoveFromServiceCartRequest {
    private Long serviceItemId;
    private Long cartItemId;
    private Boolean removeAll;
}

/**
 * DTO for Update Cart Item requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class UpdateServiceCartItemRequest {
    private Long cartItemId;
    private Integer quantity;
    private String notes;
}

/**
 * DTO for Cart Summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class ServiceCartSummaryDto {
    private Long cartId;
    private Integer itemCount;
    private Integer totalQuantity;
    private Double totalAmount;
    private Integer uniqueServicesCount;
    private Integer categoriesCount;
    private Boolean isEmpty;
    private Boolean isExpired;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private List<CategorySummary> categorySummaries;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private String categoryName;
        private Integer itemCount;
        private Double totalAmount;
        private List<String> serviceNames;
    }
}
