package com.repairapp.service;

import com.repairapp.entity.RepairService;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.RepairServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for repair service management
 */
@Service
@Transactional
public class RepairServiceService {

    private static final Logger logger = LoggerFactory.getLogger(RepairServiceService.class);

    @Autowired
    private RepairServiceRepository repairServiceRepository;

    /**
     * Get all active repair services
     */
    public List<RepairService> getAllServices() {
        return repairServiceRepository.findByIsActiveOrderByDeviceTypeAsc(true);
    }

    /**
     * Get services by device type
     */
    public List<RepairService> getServicesByDeviceType(RepairService.DeviceType deviceType) {
        return repairServiceRepository.findByDeviceTypeAndIsActive(deviceType, true);
    }

    /**
     * Get service by ID
     */
    public RepairService getServiceById(Long serviceId) {
        return repairServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairService", "id", serviceId));
    }

    /**
     * Search services by name
     */
    public List<RepairService> searchServices(String searchTerm) {
        return repairServiceRepository.findByNameContainingIgnoreCaseAndIsActive(searchTerm);
    }

    /**
     * Get services by price range
     */
    public List<RepairService> getServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return repairServiceRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Get services by maximum duration
     */
    public List<RepairService> getServicesByMaxDuration(Integer maxHours) {
        return repairServiceRepository.findByMaxDuration(maxHours);
    }

    /**
     * Get most popular services
     */
    public List<RepairService> getMostPopularServices() {
        return repairServiceRepository.findMostPopularServices();
    }

    /**
     * Create new repair service (Admin only)
     */
    public RepairService createService(RepairService service) {
        // Check if service already exists for this device type
        if (repairServiceRepository.existsByNameAndDeviceType(service.getName(), service.getDeviceType())) {
            throw new RuntimeException("Service with this name already exists for " + service.getDeviceType());
        }

        RepairService savedService = repairServiceRepository.save(service);
        logger.info("New repair service created: {} for {}", savedService.getName(), savedService.getDeviceType());
        
        return savedService;
    }

    /**
     * Update repair service (Admin only)
     */
    public RepairService updateService(Long serviceId, RepairService serviceDetails) {
        RepairService service = getServiceById(serviceId);

        if (serviceDetails.getName() != null) {
            service.setName(serviceDetails.getName());
        }
        if (serviceDetails.getDescription() != null) {
            service.setDescription(serviceDetails.getDescription());
        }
        if (serviceDetails.getBasePrice() != null) {
            service.setBasePrice(serviceDetails.getBasePrice());
        }
        if (serviceDetails.getEstimatedDurationHours() != null) {
            service.setEstimatedDurationHours(serviceDetails.getEstimatedDurationHours());
        }
        if (serviceDetails.getImageUrl() != null) {
            service.setImageUrl(serviceDetails.getImageUrl());
        }
        if (serviceDetails.getIsActive() != null) {
            service.setIsActive(serviceDetails.getIsActive());
        }

        RepairService updatedService = repairServiceRepository.save(service);
        logger.info("Repair service updated: {}", updatedService.getName());
        
        return updatedService;
    }

    /**
     * Deactivate repair service (Admin only)
     */
    public void deactivateService(Long serviceId) {
        RepairService service = getServiceById(serviceId);
        service.setIsActive(false);
        repairServiceRepository.save(service);
        
        logger.info("Repair service deactivated: {}", service.getName());
    }

    /**
     * Advanced search with filters
     */
    public List<RepairService> searchWithFilters(String searchTerm, RepairService.DeviceType deviceType,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  Integer maxDuration, String sortBy) {
        return repairServiceRepository.findWithFilters(searchTerm, deviceType, minPrice, maxPrice, maxDuration, sortBy);
    }

    /**
     * Get services with pagination
     */
    public List<RepairService> getServicesWithPagination(int page, int size, String sortBy) {
        return repairServiceRepository.findServicesWithSorting(sortBy);
    }

    /**
     * Get services by category (for future enhancement)
     */
    public List<RepairService> getServicesByCategory(String category) {
        return repairServiceRepository.findByCategoryAndIsActive(category, true);
    }

    /**
     * Get featured services
     */
    public List<RepairService> getFeaturedServices() {
        return repairServiceRepository.findFeaturedServices();
    }

    /**
     * Get services by multiple device types
     */
    public List<RepairService> getServicesByDeviceTypes(List<RepairService.DeviceType> deviceTypes) {
        return repairServiceRepository.findByDeviceTypeInAndIsActive(deviceTypes, true);
    }

    /**
     * Toggle featured status of a service (Admin only)
     */
    public RepairService toggleFeaturedStatus(Long serviceId) {
        RepairService service = getServiceById(serviceId);
        service.setIsFeatured(!service.getIsFeatured());
        RepairService updatedService = repairServiceRepository.save(service);
        
        logger.info("Service {} featured status toggled to: {}", service.getName(), service.getIsFeatured());
        return updatedService;
    }

    /**
     * Get service statistics
     */
    public Object getServiceStatistics() {
        return new Object() {
            public final long totalServices = repairServiceRepository.count();
            public final long activeServices = repairServiceRepository.countByIsActive(true);
            public final long laptopServices = repairServiceRepository.countByDeviceTypeAndIsActive(
                RepairService.DeviceType.LAPTOP, true);
            public final long mobileServices = repairServiceRepository.countByDeviceTypeAndIsActive(
                RepairService.DeviceType.MOBILE, true);
            public final long tabletServices = repairServiceRepository.countByDeviceTypeAndIsActive(
                RepairService.DeviceType.TABLET, true);
            public final long watchServices = repairServiceRepository.countByDeviceTypeAndIsActive(
                RepairService.DeviceType.WATCH, true);
            public final Object priceRanges = new Object() {
                public final long underTwentyFive = repairServiceRepository.countByPriceRangeAndIsActive(BigDecimal.ZERO, new BigDecimal("25.00"), true);
                public final long twentyFiveToFifty = repairServiceRepository.countByPriceRangeAndIsActive(new BigDecimal("25.00"), new BigDecimal("50.00"), true);
                public final long fiftyToHundred = repairServiceRepository.countByPriceRangeAndIsActive(new BigDecimal("50.00"), new BigDecimal("100.00"), true);
                public final long overHundred = repairServiceRepository.countByPriceGreaterThanAndIsActive(new BigDecimal("100.00"), true);
            };
        };
    }
}
