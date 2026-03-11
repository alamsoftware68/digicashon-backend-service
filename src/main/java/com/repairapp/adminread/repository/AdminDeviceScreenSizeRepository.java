package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceScreenSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceScreenSize entity
 */
@Repository
public interface AdminDeviceScreenSizeRepository extends JpaRepository<AdminDeviceScreenSize, Long> {

    List<AdminDeviceScreenSize> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceScreenSize> findByIdAndEnabledTrue(Long id);
}
