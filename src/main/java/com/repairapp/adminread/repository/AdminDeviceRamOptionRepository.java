package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceRamOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceRamOption entity
 */
@Repository
public interface AdminDeviceRamOptionRepository extends JpaRepository<AdminDeviceRamOption, Long> {

    List<AdminDeviceRamOption> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceRamOption> findByIdAndEnabledTrue(Long id);
}
