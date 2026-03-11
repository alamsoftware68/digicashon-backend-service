package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminBrandModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminBrandModel entity
 */
@Repository
public interface AdminBrandModelRepository extends JpaRepository<AdminBrandModel, Long> {

    List<AdminBrandModel> findByBrandIdAndEnabledTrue(Long brandId);

    List<AdminBrandModel> findByBrandIdAndBrandTypeAndEnabledTrue(Long brandId, String brandType);

    Optional<AdminBrandModel> findByIdAndEnabledTrue(Long id);
}
