package com.repairapp.repository;

import com.repairapp.entity.DeviceFunctionalIssueSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DeviceFunctionalIssueSelection entity
 */
@Repository
public interface DeviceFunctionalIssueSelectionRepository extends JpaRepository<DeviceFunctionalIssueSelection, Long> {

    /**
     * Find all functional issue selections for a device evaluation
     */
    List<DeviceFunctionalIssueSelection> findByDeviceEvaluation_Id(Long deviceEvaluationId);

    /**
     * Delete all functional issue selections for a device evaluation
     */
    void deleteByDeviceEvaluation_Id(Long deviceEvaluationId);
}
