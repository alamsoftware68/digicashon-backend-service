package com.repairapp.repository;

import com.repairapp.entity.DeviceEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DeviceEvaluation entity
 */
@Repository
public interface DeviceEvaluationRepository extends JpaRepository<DeviceEvaluation, Long> {

    /**
     * Find evaluations by user ID
     */
    List<DeviceEvaluation> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Find evaluations by user ID with pagination
     */
    Page<DeviceEvaluation> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find evaluations by status
     */
    List<DeviceEvaluation> findByStatusOrderByCreatedAtDesc(DeviceEvaluation.EvaluationStatus status);

    /**
     * Find evaluations by user and status
     */
    List<DeviceEvaluation> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, DeviceEvaluation.EvaluationStatus status);

    /**
     * Find pending evaluations older than a certain date (for expiry)
     */
    @Query("SELECT e FROM DeviceEvaluation e WHERE e.status = :status AND e.createdAt < :expiryDate")
    List<DeviceEvaluation> findExpiredEvaluations(
        @Param("status") DeviceEvaluation.EvaluationStatus status,
        @Param("expiryDate") LocalDateTime expiryDate
    );

    /**
     * Find evaluations by brand and model
     */
    List<DeviceEvaluation> findByBrandIdAndBrandModelIdOrderByCreatedAtDesc(Long brandId, Long brandModelId);

    /**
     * Find latest evaluation for a user with specific device configuration
     */
    @Query("SELECT e FROM DeviceEvaluation e WHERE e.user.id = :userId " +
           "AND e.brandId = :brandId AND e.brandModelId = :brandModelId " +
           "ORDER BY e.createdAt DESC")
    List<DeviceEvaluation> findLatestEvaluationForDevice(
        @Param("userId") Long userId,
        @Param("brandId") Long brandId,
        @Param("brandModelId") Long brandModelId,
        Pageable pageable
    );

    /**
     * Count evaluations by status
     */
    long countByStatus(DeviceEvaluation.EvaluationStatus status);

    /**
     * Count evaluations by user
     */
    long countByUser_Id(Long userId);
}
