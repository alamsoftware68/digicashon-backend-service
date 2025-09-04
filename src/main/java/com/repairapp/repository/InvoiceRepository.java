package com.repairapp.repository;

import com.repairapp.entity.Invoice;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find invoices by order
     */
    List<Invoice> findByOrderOrderByCreatedAtDesc(RepairOrder order);

    /**
     * Find invoices by user
     */
    @Query("SELECT i FROM Invoice i WHERE i.order.user = :user ORDER BY i.createdAt DESC")
    List<Invoice> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    /**
     * Find invoices by status
     */
    List<Invoice> findByStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus status);

    /**
     * Find overdue invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate < :currentDate ORDER BY i.dueDate ASC")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find invoices due within specified days
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' AND i.dueDate BETWEEN :currentDate AND :futureDate ORDER BY i.dueDate ASC")
    List<Invoice> findInvoicesDueWithinDays(@Param("currentDate") LocalDateTime currentDate, 
                                          @Param("futureDate") LocalDateTime futureDate);

    /**
     * Find paid invoices between dates
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PAID' AND i.paidDate BETWEEN :startDate AND :endDate ORDER BY i.paidDate DESC")
    List<Invoice> findPaidInvoicesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Count invoices by status
     */
    long countByStatus(Invoice.InvoiceStatus status);

    /**
     * Calculate total revenue from paid invoices
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    Double calculateTotalRevenue();

    /**
     * Calculate revenue between dates
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.paidDate BETWEEN :startDate AND :endDate")
    Double calculateRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
