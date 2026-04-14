package com.repairapp.repository;

import com.repairapp.entity.TempLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempLeadRepository extends JpaRepository<TempLead, Long> {

    List<TempLead> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("DELETE FROM TempLead t WHERE t.mobile = :mobile")
    void deleteByMobile(@Param("mobile") String mobile);

    @Modifying
    @Query("DELETE FROM TempLead t WHERE t.evaluationId = :evaluationId")
    void deleteByEvaluationId(@Param("evaluationId") Long evaluationId);
}
