package com.repairapp.repository;

import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
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
 * Repository interface for RepairOrder entity
 */
@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    /**
     * Find order by order number
     */
    Optional<RepairOrder> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user
     */
    List<RepairOrder> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find orders by user with pagination
     */
    Page<RepairOrder> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find orders by status
     */
    List<RepairOrder> findByStatus(RepairOrder.OrderStatus status);

    /**
     * Find orders by technician
     */
    List<RepairOrder> findByTechnicianOrderByCreatedAtDesc(User technician);

    /**
     * Find orders by status and technician
     */
    List<RepairOrder> findByStatusAndTechnician(RepairOrder.OrderStatus status, User technician);

    /**
     * Find orders created between dates
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<RepairOrder> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders by user and status
     */
    List<RepairOrder> findByUserAndStatusOrderByCreatedAtDesc(User user, RepairOrder.OrderStatus status);

    /**
     * Count orders by status
     */
    @Query("SELECT COUNT(o) FROM RepairOrder o WHERE o.status = :status")
    long countByStatus(@Param("status") RepairOrder.OrderStatus status);

    /**
     * Count orders by user
     */
    long countByUser(User user);

    /**
     * Find recent orders (last 30 days)
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.createdAt >= :sinceDate ORDER BY o.createdAt DESC")
    List<RepairOrder> findRecentOrders(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find orders assigned to technician with specific statuses
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.technician = :technician AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<RepairOrder> findByTechnicianAndStatusIn(@Param("technician") User technician, 
                                                @Param("statuses") List<RepairOrder.OrderStatus> statuses);

    /**
     * Find unassigned orders (no technician assigned)
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.technician IS NULL AND o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<RepairOrder> findUnassignedOrders();

    /**
     * Search orders by order number or service name
     */
    @Query("SELECT o FROM RepairOrder o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.service.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY o.createdAt DESC")
    List<RepairOrder> searchOrders(@Param("searchTerm") String searchTerm);

    /**
     * Find orders with upcoming scheduled dates
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.scheduledDate BETWEEN :startDate AND :endDate ORDER BY o.scheduledDate ASC")
    List<RepairOrder> findOrdersWithScheduledDateBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find orders by user ID (avoids detached entity issues)
     * Eagerly fetch required associations to prevent lazy loading issues
     */
    @Query("SELECT DISTINCT o FROM RepairOrder o " +
           "LEFT JOIN FETCH o.service s " +
           "LEFT JOIN FETCH o.technician t " +
           "LEFT JOIN FETCH o.address a " +
           "WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<RepairOrder> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * Find orders by user ID with pagination (avoids detached entity issues)
     * Note: Cannot use JOIN FETCH with pagination, so we'll handle this differently
     */
    @Query("SELECT o FROM RepairOrder o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<RepairOrder> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find user by ID for technician assignment (avoids detached entity issues)
     */
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findUserById(@Param("userId") Long userId);
    
    /**
     * Find order by ID with eager fetching to prevent lazy loading issues
     */
    @Query("SELECT DISTINCT o FROM RepairOrder o " +
           "LEFT JOIN FETCH o.service s " +
           "LEFT JOIN FETCH o.technician t " +
           "LEFT JOIN FETCH o.address a " +
           "WHERE o.id = :orderId")
    Optional<RepairOrder> findByIdWithAssociations(@Param("orderId") Long orderId);
    
    /**
     * Find order by order number with eager fetching to prevent lazy loading issues
     */
    @Query("SELECT DISTINCT o FROM RepairOrder o " +
           "LEFT JOIN FETCH o.service s " +
           "LEFT JOIN FETCH o.technician t " +
           "LEFT JOIN FETCH o.address a " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<RepairOrder> findByOrderNumberWithAssociations(@Param("orderNumber") String orderNumber);
}
