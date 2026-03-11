package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceProcessor entity
 */
@Repository
public interface AdminDeviceProcessorRepository extends JpaRepository<AdminDeviceProcessor, Long> {

    List<AdminDeviceProcessor> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceProcessor> findByIdAndEnabledTrue(Long id);
}
