package com.digicashon.digicashonbackendservice.repository;

import com.digicashon.digicashonbackendservice.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    /**
     * Find all service items by category ID
     */
    List<ServiceItem> findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(Long categoryId);

    /**
     * Find all active service items ordered by display order
     */
    List<ServiceItem> findByIsActiveTrueOrderByServiceCategoryDisplayOrderAscDisplayOrderAsc();

    /**
     * Search service items by title or description
     */
    @Query("SELECT si FROM ServiceItem si WHERE si.isActive = true AND " +
           "(LOWER(si.serviceTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(si.serviceDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY si.serviceCategory.displayOrder ASC, si.displayOrder ASC")
    List<ServiceItem> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Count service items by category ID
     */
    long countByServiceCategoryIdAndIsActiveTrue(Long categoryId);

    /**
     * Find service items by category name (case insensitive)
     */
    @Query("SELECT si FROM ServiceItem si WHERE LOWER(si.serviceCategory.categoryName) = LOWER(:categoryName) " +
           "AND si.isActive = true ORDER BY si.displayOrder ASC")
    List<ServiceItem> findByCategoryNameIgnoreCaseAndActive(@Param("categoryName") String categoryName);
}
