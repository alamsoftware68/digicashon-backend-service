package com.repairapp.repository;

import com.repairapp.entity.OrderItem;
import com.repairapp.entity.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entities
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order
     */
    List<OrderItem> findByOrderOrderByCreatedAtAsc(RepairOrder order);

    /**
     * Find order items by order ID
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId ORDER BY oi.createdAt ASC")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find order items by service ID
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.service.id = :serviceId")
    List<OrderItem> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Delete all order items for a specific order
     */
    void deleteByOrder(RepairOrder order);
}
