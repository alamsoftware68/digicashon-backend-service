package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminSuperCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Read-only repository for AdminSuperCategory entity
 */
@Repository
public interface AdminSuperCategoryRepository extends JpaRepository<AdminSuperCategory, Long> {

    List<AdminSuperCategory> findByEnabledTrueOrderByIdAsc();
}
