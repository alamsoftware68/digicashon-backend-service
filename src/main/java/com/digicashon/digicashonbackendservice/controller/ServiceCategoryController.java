package com.digicashon.digicashonbackendservice.controller;

import com.digicashon.digicashonbackendservice.dto.ServiceCategoryDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCategoryGroupDto;
import com.digicashon.digicashonbackendservice.dto.ServiceCategoryResponseDto;
import com.digicashon.digicashonbackendservice.dto.ServiceItemDto;
import com.repairapp.dto.response.ApiResponse;
import com.digicashon.digicashonbackendservice.service.ServiceCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/service-categories")
@Tag(name = "Service Categories", description = "Laptop repair service categories management")
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceCategoryController {

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    /**
     * Get all service categories grouped by category name
     */
    @GetMapping
    @Operation(summary = "Get all service categories", description = "Retrieve all laptop repair service categories grouped by category name")
    public ResponseEntity<ApiResponse<ServiceCategoryResponseDto>> getAllServiceCategories() {
        try {
            log.info("API Request: Get all service categories");
            
            ServiceCategoryResponseDto response = serviceCategoryService.getAllServiceCategories();
            
            if (response.getTotalServices() == 0) {
                return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "No service categories found", 
                    response
                ));
            }
            
            log.info("API Response: Successfully retrieved {} categories with {} services", 
                    response.getTotalCategories(), response.getTotalServices());
                    
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Successfully retrieved %d service categories with %d total services", 
                            response.getTotalCategories(), response.getTotalServices()), 
                response
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving service categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve service categories: " + e.getMessage(), null));
        }
    }

    /**
     * Get services by specific category name
     */
    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get services by category", description = "Retrieve all services for a specific category")
    public ResponseEntity<ApiResponse<ServiceCategoryGroupDto>> getServicesByCategory(
            @Parameter(description = "Category name (e.g., 'Home Visit', 'Repair Service')")
            @PathVariable String categoryName) {
        try {
            log.info("API Request: Get services for category: {}", categoryName);
            
            ServiceCategoryGroupDto response = serviceCategoryService.getServicesByCategory(categoryName);
            
            if (response.getServiceCount() == 0) {
                return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    String.format("No services found for category: %s", categoryName), 
                    response
                ));
            }
            
            log.info("API Response: Found {} services for category: {}", response.getServiceCount(), categoryName);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Successfully retrieved %d services for category: %s", 
                            response.getServiceCount(), categoryName), 
                response
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving services for category: {}", categoryName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve services for category: " + e.getMessage(), null));
        }
    }

    /**
     * Get all category names
     */
    @GetMapping("/names")
    @Operation(summary = "Get all category names", description = "Retrieve list of all available category names")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategoryNames() {
        try {
            log.info("API Request: Get all category names");
            
            List<String> categoryNames = serviceCategoryService.getAllCategoryNames();
            
            log.info("API Response: Found {} category names", categoryNames.size());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Successfully retrieved %d category names", categoryNames.size()), 
                categoryNames
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving category names", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve category names: " + e.getMessage(), null));
        }
    }

    /**
     * Search services by title or description
     */
    @GetMapping("/search")
    @Operation(summary = "Search services", description = "Search services by title or description")
    public ResponseEntity<ApiResponse<List<ServiceItemDto>>> searchServices(
            @Parameter(description = "Search term to find in service title or description")
            @RequestParam String query) {
        try {
            log.info("API Request: Search services with query: {}", query);
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Search query cannot be empty", null));
            }
            
            List<ServiceItemDto> searchResults = serviceCategoryService.searchServices(query.trim());
            
            log.info("API Response: Found {} services matching query: {}", searchResults.size(), query);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Found %d services matching query: %s", searchResults.size(), query), 
                searchResults
            ));
            
        } catch (Exception e) {
            log.error("Error searching services with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to search services: " + e.getMessage(), null));
        }
    }

    /**
     * Get service counts by category
     */
    @GetMapping("/counts")
    @Operation(summary = "Get service counts by category", description = "Get the count of services in each category")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getServiceCountsByCategory() {
        try {
            log.info("API Request: Get service counts by category");
            
            Map<String, Long> counts = serviceCategoryService.getServiceCountByCategory();
            
            log.info("API Response: Retrieved service counts for {} categories", counts.size());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Successfully retrieved service counts for %d categories", counts.size()), 
                counts
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving service counts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve service counts: " + e.getMessage(), null));
        }
    }

    /**
     * Get a single service by ID
     */
    @GetMapping("/service/{id}")
    @Operation(summary = "Get service by ID", description = "Retrieve a specific service by its ID")
    public ResponseEntity<ApiResponse<ServiceItemDto>> getServiceById(
            @Parameter(description = "Service ID")
            @PathVariable Long id) {
        try {
            log.info("API Request: Get service by ID: {}", id);
            
            ServiceItemDto service = serviceCategoryService.getServiceById(id);
            
            if (service == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, String.format("Service not found with ID: %d", id), null));
            }
            
            log.info("API Response: Successfully retrieved service with ID: {}", id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                String.format("Successfully retrieved service: %s", service.getServiceTitle()), 
                service
            ));
            
        } catch (Exception e) {
            log.error("Error retrieving service with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve service: " + e.getMessage(), null));
        }
    }

    /**
     * Health check endpoint for service categories
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if service categories API is working")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            List<String> categoryNames = serviceCategoryService.getAllCategoryNames();
            Map<String, Long> counts = serviceCategoryService.getServiceCountByCategory();
            
            Map<String, Object> healthData = Map.of(
                "status", "UP",
                "totalCategories", categoryNames.size(),
                "totalServices", counts.values().stream().mapToLong(Long::longValue).sum(),
                "availableCategories", categoryNames
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Service Categories API is healthy", 
                healthData
            ));
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponse<>(false, "Service Categories API is unhealthy: " + e.getMessage(), null));
        }
    }
}
