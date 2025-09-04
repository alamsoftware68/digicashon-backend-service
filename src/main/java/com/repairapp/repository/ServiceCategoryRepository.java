package com.repairapp.repository;

import com.repairapp.entity.ServiceCategory;
import com.repairapp.entity.RepairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ServiceCategory entities
 */
@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    /**
     * Find all active categories
     */
    List<ServiceCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find categories by device type
     */
    List<ServiceCategory> findByDeviceTypeAndIsActiveTrueOrderByDisplayOrderAsc(RepairService.DeviceType deviceType);

    /**
     * Find category by name
     */
    Optional<ServiceCategory> findByNameIgnoreCase(String name);

    /**
     * Find categories with services
     */
    @Query("SELECT DISTINCT c FROM ServiceCategory c LEFT JOIN FETCH c.services s WHERE c.isActive = true AND (s.isActive = true OR s IS NULL) ORDER BY c.displayOrder ASC")
    List<ServiceCategory> findActiveCategoriesWithServices();

    /**
     * Find category by name and device type
     */
    Optional<ServiceCategory> findByNameIgnoreCaseAndDeviceType(String name, RepairService.DeviceType deviceType);

    /**
     * Count services in category
     */
    @Query("SELECT COUNT(s) FROM RepairService s WHERE s.category.id = :categoryId AND s.isActive = true")
    Long countActiveServicesInCategory(@Param("categoryId") Long categoryId);
}
