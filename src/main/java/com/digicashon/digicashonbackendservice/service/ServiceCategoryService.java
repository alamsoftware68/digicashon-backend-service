package com.digicashon.digicashonbackendservice.service;

import com.digicashon.digicashonbackendservice.dto.ServiceCategoryDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCategoryGroupDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCategoryResponseDto;
import com.digicashon.digicashonbackendservice.dto.ServiceItemDto;
import com.digicashon.digicashonbackendservice.entity.ServiceCategory;
import com.digicashon.digicashonbackendservice.entity.ServiceItem;
import com.digicashon.digicashonbackendservice.repository.ServiceCategoryRepository;
import com.digicashon.digicashonbackendservice.repository.ServiceItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;
    
    @Autowired
    private ServiceItemRepository serviceItemRepository;

    /**
     * Get all service categories with their services
     */
    @Cacheable("serviceCategories")
    public ServiceCategoryResponseDto getAllServiceCategories() {
        log.info("Fetching all service categories with services");

        List<ServiceCategory> categories = serviceCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        List<ServiceCategoryGroupDto> categoryGroups = categories.stream()
                .map(category -> {
                    List<ServiceItem> serviceItems = serviceItemRepository
                            .findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(category.getId());
                    
                    List<ServiceItemDto> services = serviceItems.stream()
                            .map(this::convertServiceItemToDto)
                            .collect(Collectors.toList());
                    
                    return new ServiceCategoryGroupDto(
                            category.getId(),
                            category.getCategoryName(),
                            category.getCategoryDescription(),
                            category.getCategoryIcon(),
                            category.getDisplayOrder(),
                            category.getBulletPoint1(),
                            category.getBulletPoint2(),
                            services

                    );
                })
                .collect(Collectors.toList());

        int totalServices = categoryGroups.stream().mapToInt(group -> group.getServiceCount()).sum();
        
        log.info("Retrieved {} categories with {} total services", categoryGroups.size(), totalServices);

        return new ServiceCategoryResponseDto(categoryGroups);
    }

    /**
     * Get services by specific category name
     */
    @Cacheable("serviceCategoriesByName")
    public ServiceCategoryGroupDto getServicesByCategory(String categoryName) {
        log.info("Fetching services for category: {}", categoryName);

        ServiceCategory category = serviceCategoryRepository
                .findByCategoryNameIgnoreCaseAndActive(categoryName)
                .orElse(null);

        if (category == null) {
            log.warn("No category found with name: {}", categoryName);
            return new ServiceCategoryGroupDto(null, categoryName, null, null,null,0,null, null, List.of());
        }

        List<ServiceItem> serviceItems = serviceItemRepository
                .findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(category.getId());
        
        List<ServiceItemDto> services = serviceItems.stream()
                .map(this::convertServiceItemToDto)
                .collect(Collectors.toList());

        log.info("Found {} services for category: {}", services.size(), categoryName);
        
        return new ServiceCategoryGroupDto(
                category.getId(),
                category.getCategoryName(),
                category.getCategoryDescription(),
                category.getCategoryIcon(),
                category.getDisplayOrder(),
                category.getBulletPoint1(),
                category.getBulletPoint2(),
                services
        );
    }

    /**
     * Get all category names
     */
    @Cacheable("categoryNames")
    public List<String> getAllCategoryNames() {
        log.info("Fetching all category names");
        
        List<String> categoryNames = serviceCategoryRepository.findAllActiveCategoryNames();
        
        log.info("Found {} category names", categoryNames.size());
        return categoryNames;
    }

    /**
     * Search services by title or description
     */
    public List<ServiceItemDto> searchServices(String searchTerm) {
        log.info("Searching services with term: {}", searchTerm);
        
        List<ServiceItem> searchResults = serviceItemRepository.searchByTitleOrDescription(searchTerm);
        
        List<ServiceItemDto> results = searchResults.stream()
                .map(this::convertServiceItemToDto)
                .collect(Collectors.toList());
        
        log.info("Found {} services matching search term: {}", results.size(), searchTerm);
        return results;
    }

    /**
     * Get service count by category
     */
    public Map<String, Long> getServiceCountByCategory() {
        log.info("Fetching service counts by category");
        
        List<ServiceCategory> categories = serviceCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        
        Map<String, Long> countMap = categories.stream()
                .collect(Collectors.toMap(
                    ServiceCategory::getCategoryName,
                    category -> serviceItemRepository.countByServiceCategoryIdAndIsActiveTrue(category.getId())
                ));
        
        log.info("Service counts calculated for {} categories", countMap.size());
        return countMap;
    }

    /**
     * Get a single service item by ID
     */
    public ServiceItemDto getServiceById(Long id) {
        log.info("Fetching service with ID: {}", id);
        
        return serviceItemRepository.findById(id)
                .filter(ServiceItem::getIsActive)
                .map(this::convertServiceItemToDto)
                .orElse(null);
    }
    
    /**
     * Get category by ID
     */
    public ServiceCategoryDto getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        
        return serviceCategoryRepository.findById(id)
                .filter(ServiceCategory::getIsActive)
                .map(this::convertCategoryToDto)
                .orElse(null);
    }

    /**
     * Convert ServiceItem entity to DTO
     */
    private ServiceItemDto convertServiceItemToDto(ServiceItem entity) {
        return new ServiceItemDto(
                entity.getId(),
                entity.getServiceTitle(),
                entity.getServiceDescription(),
                entity.getBulletPoint1(),
                entity.getBulletPoint2(),
                entity.getBulletPoint3(),
                entity.getBulletPoint4(),
                entity.getServiceIcon(),
                entity.getPrice(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * Convert ServiceCategory entity to DTO
     */
    private ServiceCategoryDto convertCategoryToDto(ServiceCategory entity) {
        return new ServiceCategoryDto(
                entity.getId(),
                entity.getCategoryName(),
                entity.getCategoryDescription(),
                entity.getCategoryIcon(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
