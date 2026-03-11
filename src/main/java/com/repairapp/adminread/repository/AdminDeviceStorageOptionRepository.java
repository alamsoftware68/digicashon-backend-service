package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceStorageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceStorageOption entity
 */
@Repository
public interface AdminDeviceStorageOptionRepository extends JpaRepository<AdminDeviceStorageOption, Long> {

    List<AdminDeviceStorageOption> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceStorageOption> findByIdAndEnabledTrue(Long id);
}
