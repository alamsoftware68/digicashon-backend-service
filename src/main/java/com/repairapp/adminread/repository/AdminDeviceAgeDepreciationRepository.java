package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceAgeDepreciation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceAgeDepreciation entity
 */
@Repository
public interface AdminDeviceAgeDepreciationRepository extends JpaRepository<AdminDeviceAgeDepreciation, Long> {

    List<AdminDeviceAgeDepreciation> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceAgeDepreciation> findByAgeCodeAndEnabledTrue(String ageCode);

    Optional<AdminDeviceAgeDepreciation> findByIdAndEnabledTrue(Long id);
}
