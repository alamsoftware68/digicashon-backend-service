package com.repairapp.repository;

import com.repairapp.entity.RepairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for RepairService entity
 */
@Repository
public interface RepairServiceRepository extends JpaRepository<RepairService, Long> {

    /**
     * Find services by device type
     */
    List<RepairService> findByDeviceType(RepairService.DeviceType deviceType);

    /**
     * Find active services by device type
     */
    List<RepairService> findByDeviceTypeAndIsActive(RepairService.DeviceType deviceType, Boolean isActive);

    /**
     * Find all active services
     */
    List<RepairService> findByIsActiveOrderByDeviceTypeAsc(Boolean isActive);

    /**
     * Search services by name (case insensitive)
     */
    @Query("SELECT s FROM RepairService s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    List<RepairService> findByNameContainingIgnoreCaseAndIsActive(@Param("name") String name);

    /**
     * Find services by price range
     */
    @Query("SELECT s FROM RepairService s WHERE s.basePrice BETWEEN :minPrice AND :maxPrice AND s.isActive = true ORDER BY s.basePrice ASC")
    List<RepairService> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find services by estimated duration
     */
    @Query("SELECT s FROM RepairService s WHERE s.estimatedDurationHours <= :maxHours AND s.isActive = true ORDER BY s.estimatedDurationHours ASC")
    List<RepairService> findByMaxDuration(@Param("maxHours") Integer maxHours);

    /**
     * Find most popular services (by order count)
     */
    @Query("SELECT s FROM RepairService s LEFT JOIN s.orders o WHERE s.isActive = true GROUP BY s ORDER BY COUNT(o) DESC")
    List<RepairService> findMostPopularServices();

    /**
     * Count services by device type
     */
    long countByDeviceTypeAndIsActive(RepairService.DeviceType deviceType, Boolean isActive);

    /**
     * Check if service name exists
     */
    boolean existsByNameAndDeviceType(String name, RepairService.DeviceType deviceType);

    /**
     * Advanced search with filters
     */
    @Query("SELECT s FROM RepairService s WHERE " +
           "(:searchTerm IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:deviceType IS NULL OR s.deviceType = :deviceType) AND " +
           "(:minPrice IS NULL OR s.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.basePrice <= :maxPrice) AND " +
           "(:maxDuration IS NULL OR s.estimatedDurationHours <= :maxDuration) AND " +
           "s.isActive = true " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price_asc' THEN s.basePrice END ASC, " +
           "CASE WHEN :sortBy = 'price_desc' THEN s.basePrice END DESC, " +
           "CASE WHEN :sortBy = 'duration_asc' THEN s.estimatedDurationHours END ASC, " +
           "CASE WHEN :sortBy = 'duration_desc' THEN s.estimatedDurationHours END DESC, " +
           "CASE WHEN :sortBy = 'name_asc' THEN s.name END ASC, " +
           "CASE WHEN :sortBy = 'name_desc' THEN s.name END DESC, " +
           "s.createdAt DESC")
    List<RepairService> findWithFilters(@Param("searchTerm") String searchTerm,
                                        @Param("deviceType") RepairService.DeviceType deviceType,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("maxDuration") Integer maxDuration,
                                        @Param("sortBy") String sortBy);

    /**
     * Find services with pagination (using Spring Data Pageable)
     */
    @Query("SELECT s FROM RepairService s WHERE s.isActive = true " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price_asc' THEN s.basePrice END ASC, " +
           "CASE WHEN :sortBy = 'price_desc' THEN s.basePrice END DESC, " +
           "CASE WHEN :sortBy = 'duration_asc' THEN s.estimatedDurationHours END ASC, " +
           "CASE WHEN :sortBy = 'duration_desc' THEN s.estimatedDurationHours END DESC, " +
           "CASE WHEN :sortBy = 'name_asc' THEN s.name END ASC, " +
           "CASE WHEN :sortBy = 'name_desc' THEN s.name END DESC, " +
           "s.createdAt DESC")
    List<RepairService> findServicesWithSorting(@Param("sortBy") String sortBy);

    /**
     * Find services by category (using relationship)
     */
    @Query("SELECT s FROM RepairService s JOIN s.category c WHERE c.name = :categoryName AND s.isActive = :isActive")
    List<RepairService> findByCategoryAndIsActive(@Param("categoryName") String categoryName, @Param("isActive") Boolean isActive);

    /**
     * Find featured services
     */
    @Query("SELECT s FROM RepairService s WHERE s.isActive = true AND s.isFeatured = true ORDER BY s.createdAt DESC")
    List<RepairService> findFeaturedServices();

    /**
     * Find services by multiple device types
     */
    List<RepairService> findByDeviceTypeInAndIsActive(List<RepairService.DeviceType> deviceTypes, Boolean isActive);

    /**
     * Count by active status
     */
    long countByIsActive(Boolean isActive);

    /**
     * Count services by price range
     */
    @Query("SELECT COUNT(s) FROM RepairService s WHERE s.basePrice BETWEEN :minPrice AND :maxPrice AND s.isActive = :isActive")
    long countByPriceRangeAndIsActive(@Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      @Param("isActive") Boolean isActive);

    /**
     * Count services by price greater than
     */
    @Query("SELECT COUNT(s) FROM RepairService s WHERE s.basePrice > :price AND s.isActive = :isActive")
    long countByPriceGreaterThanAndIsActive(@Param("price") BigDecimal price,
                                            @Param("isActive") Boolean isActive);
}
