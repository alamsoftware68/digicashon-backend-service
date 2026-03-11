package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceConditionMultiplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceConditionMultiplier entity
 */
@Repository
public interface AdminDeviceConditionMultiplierRepository extends JpaRepository<AdminDeviceConditionMultiplier, Long> {

    List<AdminDeviceConditionMultiplier> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceConditionMultiplier> findByConditionCodeAndEnabledTrue(String conditionCode);

    Optional<AdminDeviceConditionMultiplier> findByIdAndEnabledTrue(Long id);
}
