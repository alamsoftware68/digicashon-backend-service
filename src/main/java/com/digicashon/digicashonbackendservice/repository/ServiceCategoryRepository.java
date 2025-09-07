package com.digicashon.digicashonbackendservice.repository;

import com.digicashon.digicashonbackendservice.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    /**
     * Find all active service categories ordered by display order
     */
    List<ServiceCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find service category by name (case insensitive)
     */
    @Query("SELECT sc FROM LaptopServiceCategory sc WHERE LOWER(sc.categoryName) = LOWER(:categoryName) AND sc.isActive = true")
    Optional<ServiceCategory> findByCategoryNameIgnoreCaseAndActive(@Param("categoryName") String categoryName);

    /**
     * Get all active category names
     */
    @Query("SELECT sc.categoryName FROM LaptopServiceCategory sc WHERE sc.isActive = true ORDER BY sc.displayOrder ASC")
    List<String> findAllActiveCategoryNames();

    /**
     * Count active service categories
     */
    long countByIsActiveTrue();

    /**
     * Find category by exact name
     */
    Optional<ServiceCategory> findByCategoryNameAndIsActiveTrue(String categoryName);
}
