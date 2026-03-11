package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminBrand entity
 */
@Repository
public interface AdminBrandRepository extends JpaRepository<AdminBrand, Long> {

    List<AdminBrand> findByEnabledTrueOrderByNameAsc();

    List<AdminBrand> findByBrandTypeAndEnabledTrue(String brandType);

    Optional<AdminBrand> findByIdAndEnabledTrue(Long id);
}
