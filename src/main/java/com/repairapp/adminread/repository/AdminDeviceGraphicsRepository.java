package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceGraphics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceGraphics entity
 */
@Repository
public interface AdminDeviceGraphicsRepository extends JpaRepository<AdminDeviceGraphics, Long> {

    List<AdminDeviceGraphics> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceGraphics> findByIdAndEnabledTrue(Long id);
}
