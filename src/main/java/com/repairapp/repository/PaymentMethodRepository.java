package com.repairapp.repository;

import com.repairapp.entity.PaymentMethod;
import com.repairapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentMethod entity
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find payment methods by user
     */
    List<PaymentMethod> findByUserAndIsActiveOrderByIsDefaultDescCreatedAtDesc(User user, Boolean isActive);

    /**
     * Find user's default payment method
     */
    Optional<PaymentMethod> findByUserAndIsDefaultAndIsActive(User user, Boolean isDefault, Boolean isActive);

    /**
     * Find payment methods by user and type
     */
    List<PaymentMethod> findByUserAndTypeAndIsActive(User user, PaymentMethod.PaymentType type, Boolean isActive);

    /**
     * Count active payment methods by user
     */
    long countByUserAndIsActive(User user, Boolean isActive);
}
