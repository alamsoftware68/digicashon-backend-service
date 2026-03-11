package com.repairapp.adminread.repository;

import com.repairapp.adminread.entity.AdminDeviceFunctionalIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only repository for AdminDeviceFunctionalIssue entity
 */
@Repository
public interface AdminDeviceFunctionalIssueRepository extends JpaRepository<AdminDeviceFunctionalIssue, Long> {

    List<AdminDeviceFunctionalIssue> findByEnabledTrueOrderByDisplayOrderAsc();

    Optional<AdminDeviceFunctionalIssue> findByIssueCodeAndEnabledTrue(String issueCode);

    Optional<AdminDeviceFunctionalIssue> findByIdAndEnabledTrue(Long id);
}
