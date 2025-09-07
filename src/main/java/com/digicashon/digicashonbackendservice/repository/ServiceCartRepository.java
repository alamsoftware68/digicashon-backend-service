package com.digicashon.digicashonbackendservice.repository;

import com.digicashon.digicashonbackendservice.entity.ServiceCart;
import com.repairapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCartRepository extends JpaRepository<ServiceCart, Long> {

    /**
     * Find active cart by session ID for guest users
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.sessionId = :sessionId AND sc.isActive = true")
    Optional<ServiceCart> findBySessionIdAndIsActiveTrue(@Param("sessionId") String sessionId);

    /**
     * Find active cart by user ID for authenticated users
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.user.id = :userId AND sc.isActive = true")
    Optional<ServiceCart> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * Find active cart by either session ID or user ID
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE " +
           "(sc.sessionId = :sessionId OR sc.user.id = :userId) AND sc.isActive = true " +
           "ORDER BY sc.updatedAt DESC")
    List<ServiceCart> findBySessionIdOrUserIdAndIsActiveTrue(@Param("sessionId") String sessionId, 
                                                           @Param("userId") Long userId);

    /**
     * Find expired carts
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.expiresAt < :currentTime AND sc.isActive = true")
    List<ServiceCart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find empty carts
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.itemCount = 0 AND sc.isActive = true")
    List<ServiceCart> findEmptyCarts();

    /**
     * Find carts older than specified days
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.createdAt < :cutoffDate")
    List<ServiceCart> findCartsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recent carts within specified hours
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.createdAt >= :sinceTime AND sc.isActive = true")
    List<ServiceCart> findRecentCarts(@Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Count active carts
     */
    @Query("SELECT COUNT(sc) FROM ServiceCart sc WHERE sc.isActive = true")
    long countActiveCarts();

    /**
     * Find abandoned carts (not updated for specified hours)
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.updatedAt < :cutoffTime AND sc.isActive = true AND sc.itemCount > 0")
    List<ServiceCart> findAbandonedCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Deactivate cart by ID
     */
    @Modifying
    @Query("UPDATE ServiceCart sc SET sc.isActive = false WHERE sc.id = :cartId")
    int deactivateCart(@Param("cartId") Long cartId);

    /**
     * Deactivate expired carts
     */
    @Modifying
    @Query("UPDATE ServiceCart sc SET sc.isActive = false WHERE sc.expiresAt < :currentTime")
    int deactivateExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Delete empty and inactive carts
     */
    @Modifying
    @Query("DELETE FROM ServiceCart sc WHERE sc.itemCount = 0 AND sc.isActive = false")
    int deleteEmptyInactiveCarts();

    /**
     * Find cart by ID with items
     */
    @Query("SELECT DISTINCT sc FROM ServiceCart sc LEFT JOIN FETCH sc.serviceCartItems WHERE sc.id = :cartId")
    Optional<ServiceCart> findByIdWithItems(@Param("cartId") Long cartId);

    /**
     * Check if cart belongs to user or session
     */
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM ServiceCart sc " +
           "WHERE sc.id = :cartId AND (sc.sessionId = :sessionId OR sc.user.id = :userId)")
    boolean existsByIdAndOwnership(@Param("cartId") Long cartId, 
                                 @Param("sessionId") String sessionId, 
                                 @Param("userId") Long userId);

    /**
     * Transfer session cart to user
     */
    @Modifying
    @Query("UPDATE ServiceCart sc SET sc.user = :user, sc.sessionId = null " +
           "WHERE sc.sessionId = :sessionId AND sc.isActive = true")
    int transferSessionCartToUser(@Param("sessionId") String sessionId, @Param("user") User user);

    /**
     * Find all carts by user (for merging purposes)
     */
    @Query("SELECT sc FROM ServiceCart sc WHERE sc.user.id = :userId AND sc.isActive = true")
    List<ServiceCart> findAllByUserId(@Param("userId") Long userId);

    /**
     * Find carts by user with items
     */
    @Query("SELECT DISTINCT sc FROM ServiceCart sc LEFT JOIN FETCH sc.serviceCartItems " +
           "WHERE sc.user.id = :userId AND sc.isActive = true")
    List<ServiceCart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Find carts by session with items
     */
    @Query("SELECT DISTINCT sc FROM ServiceCart sc LEFT JOIN FETCH sc.serviceCartItems " +
           "WHERE sc.sessionId = :sessionId AND sc.isActive = true")
    List<ServiceCart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    /**
     * Count items in user's cart
     */
    @Query("SELECT COALESCE(SUM(sc.itemCount), 0) FROM ServiceCart sc " +
           "WHERE sc.user.id = :userId AND sc.isActive = true")
    long countItemsByUserId(@Param("userId") Long userId);

    /**
     * Count items in session cart
     */
    @Query("SELECT COALESCE(SUM(sc.itemCount), 0) FROM ServiceCart sc " +
           "WHERE sc.sessionId = :sessionId AND sc.isActive = true")
    long countItemsBySessionId(@Param("sessionId") String sessionId);

    /**
     * Get total amount for user's cart
     */
    @Query("SELECT COALESCE(SUM(sc.totalAmount), 0) FROM ServiceCart sc " +
           "WHERE sc.user.id = :userId AND sc.isActive = true")
    double getTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * Get total amount for session cart
     */
    @Query("SELECT COALESCE(SUM(sc.totalAmount), 0) FROM ServiceCart sc " +
           "WHERE sc.sessionId = :sessionId AND sc.isActive = true")
    double getTotalAmountBySessionId(@Param("sessionId") String sessionId);
}
