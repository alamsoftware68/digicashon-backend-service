package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.RepairService;
import com.repairapp.service.RepairServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repair services REST controller
 */
@Tag(name = "Repair Services", description = "Repair service management APIs")
@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceController {

    @Autowired
    private RepairServiceService repairServiceService;

    /**
     * Get all repair services
     */
    @Operation(summary = "Get all services", description = "Get all available repair services")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepairService>>> getAllServices() {
        List<RepairService> services = repairServiceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    /**
     * Get services by device type
     */
    @Operation(summary = "Get services by device type", description = "Get repair services for a specific device type")
    @GetMapping("/device/{deviceType}")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesByDeviceType(
            @PathVariable RepairService.DeviceType deviceType) {
        List<RepairService> services = repairServiceService.getServicesByDeviceType(deviceType);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    /**
     * Get service by ID
     */
    @Operation(summary = "Get service by ID", description = "Get a specific repair service by ID")
    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<RepairService>> getServiceById(@PathVariable Long serviceId) {
        RepairService service = repairServiceService.getServiceById(serviceId);
        return ResponseEntity.ok(ApiResponse.success("Service retrieved successfully", service));
    }

    /**
     * Search services
     */
    @Operation(summary = "Search services", description = "Search repair services by name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RepairService>>> searchServices(@RequestParam String q) {
        List<RepairService> services = repairServiceService.searchServices(q);
        return ResponseEntity.ok(ApiResponse.success("Search completed", services));
    }

    /**
     * Get services by price range
     */
    @Operation(summary = "Get services by price range", description = "Get services within a specific price range")
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<RepairService> services = repairServiceService.getServicesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    /**
     * Get services by duration
     */
    @Operation(summary = "Get services by duration", description = "Get services that can be completed within specified hours")
    @GetMapping("/duration")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesByDuration(@RequestParam Integer maxHours) {
        List<RepairService> services = repairServiceService.getServicesByMaxDuration(maxHours);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved successfully", services));
    }

    /**
     * Get popular services
     */
    @Operation(summary = "Get popular services", description = "Get most popular repair services")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<RepairService>>> getPopularServices() {
        List<RepairService> services = repairServiceService.getMostPopularServices();
        return ResponseEntity.ok(ApiResponse.success("Popular services retrieved successfully", services));
    }

    /**
     * Advanced search with filters
     */
    @Operation(summary = "Advanced search with filters", description = "Search services with advanced filters")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<RepairService>>> searchWithFilters(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) RepairService.DeviceType deviceType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false, defaultValue = "name_asc") String sortBy) {
        List<RepairService> services = repairServiceService.searchWithFilters(
                searchTerm, deviceType, minPrice, maxPrice, maxDuration, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Filtered services retrieved successfully", services));
    }

    /**
     * Get services with pagination
     */
    @Operation(summary = "Get services with pagination", description = "Get services with pagination support")
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name_asc") String sortBy) {
        List<RepairService> services = repairServiceService.getServicesWithPagination(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Paginated services retrieved successfully", services));
    }

    /**
     * Get services by category
     */
    @Operation(summary = "Get services by category", description = "Get services by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesByCategory(@PathVariable String category) {
        List<RepairService> services = repairServiceService.getServicesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Services by category retrieved successfully", services));
    }

    /**
     * Get featured services
     */
    @Operation(summary = "Get featured services", description = "Get featured repair services")
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<RepairService>>> getFeaturedServices() {
        List<RepairService> services = repairServiceService.getFeaturedServices();
        return ResponseEntity.ok(ApiResponse.success("Featured services retrieved successfully", services));
    }

    /**
     * Get services by multiple device types
     */
    @Operation(summary = "Get services by device types", description = "Get services by multiple device types")
    @PostMapping("/device-types")
    public ResponseEntity<ApiResponse<List<RepairService>>> getServicesByDeviceTypes(
            @RequestBody List<RepairService.DeviceType> deviceTypes) {
        List<RepairService> services = repairServiceService.getServicesByDeviceTypes(deviceTypes);
        return ResponseEntity.ok(ApiResponse.success("Services by device types retrieved successfully", services));
    }

    /**
     * Create new repair service (Admin only)
     */
    @Operation(summary = "Create service", description = "Create a new repair service (Admin only)")
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairService>> createService(@Valid @RequestBody RepairService service) {
        RepairService createdService = repairServiceService.createService(service);
        return ResponseEntity.ok(ApiResponse.success("Service created successfully", createdService));
    }

    /**
     * Update repair service (Admin only)
     */
    @Operation(summary = "Update service", description = "Update an existing repair service (Admin only)")
    @PutMapping("/admin/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairService>> updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody RepairService serviceDetails) {
        RepairService updatedService = repairServiceService.updateService(serviceId, serviceDetails);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", updatedService));
    }

    /**
     * Deactivate repair service (Admin only)
     */
    @Operation(summary = "Deactivate service", description = "Deactivate a repair service (Admin only)")
    @DeleteMapping("/admin/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable Long serviceId) {
        repairServiceService.deactivateService(serviceId);
        return ResponseEntity.ok(ApiResponse.success("Service deactivated successfully"));
    }

    /**
     * Toggle service featured status (Admin only)
     */
    @Operation(summary = "Toggle featured status", description = "Toggle service featured status (Admin only)")
    @PutMapping("/admin/{serviceId}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RepairService>> toggleFeaturedStatus(@PathVariable Long serviceId) {
        RepairService service = repairServiceService.getServiceById(serviceId);
        service.setIsFeatured(!service.getIsFeatured());
        RepairService updatedService = repairServiceService.updateService(serviceId, service);
        return ResponseEntity.ok(ApiResponse.success("Service featured status updated", updatedService));
    }

    /**
     * Get service statistics
     */
    @Operation(summary = "Get service statistics", description = "Get repair service statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getServiceStatistics() {
        Object statistics = repairServiceService.getServiceStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }
}
