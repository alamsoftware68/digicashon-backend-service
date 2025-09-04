package com.repairapp.repository;

import com.repairapp.entity.Cart;
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
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Find active cart by session ID (for guest users)
    Optional<Cart> findBySessionIdAndIsActiveTrue(String sessionId);
    
    // Find active cart by user (for authenticated users)
    Optional<Cart> findByUserAndIsActiveTrue(User user);
    
    // Find cart by user ID
    Optional<Cart> findByUserIdAndIsActiveTrue(Long userId);
    
    // Find all expired carts for cleanup
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :currentTime AND c.isActive = true")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);
    
    // Find carts by session ID (including inactive)
    List<Cart> findBySessionId(String sessionId);
    
    // Find carts by user (including inactive)
    List<Cart> findByUser(User user);
    
    // Count active carts by user
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user = :user AND c.isActive = true")
    long countActiveCartsByUser(@Param("user") User user);
    
    // Count active carts by session
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.sessionId = :sessionId AND c.isActive = true")
    long countActiveCartsBySession(@Param("sessionId") String sessionId);
    
    // Find carts with items count greater than specified value
    @Query("SELECT c FROM Cart c WHERE c.itemCount > :minItems AND c.isActive = true")
    List<Cart> findCartsWithMinItems(@Param("minItems") Integer minItems);
    
    // Update cart expiry time
    @Modifying
    @Query("UPDATE Cart c SET c.expiresAt = :newExpiryTime WHERE c.id = :cartId")
    int updateCartExpiry(@Param("cartId") Long cartId, @Param("newExpiryTime") LocalDateTime newExpiryTime);
    
    // Deactivate expired carts (bulk operation)
    @Modifying
    @Query("UPDATE Cart c SET c.isActive = false WHERE c.expiresAt < :currentTime AND c.isActive = true")
    int deactivateExpiredCarts(@Param("currentTime") LocalDateTime currentTime);
    
    // Find recently created carts (within specified hours)
    @Query("SELECT c FROM Cart c WHERE c.createdAt >= :since AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Cart> findRecentCarts(@Param("since") LocalDateTime since);
    
    // Find carts by total amount range
    @Query("SELECT c FROM Cart c WHERE c.totalAmount BETWEEN :minAmount AND :maxAmount AND c.isActive = true")
    List<Cart> findCartsByAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);
    
    // Find abandoned carts (created before specified time but still active)
    @Query("SELECT c FROM Cart c WHERE c.createdAt < :abandonedTime AND c.isActive = true AND c.itemCount > 0")
    List<Cart> findAbandonedCarts(@Param("abandonedTime") LocalDateTime abandonedTime);
    
    // Transfer cart from session to user (when user logs in)
    @Modifying
    @Query("UPDATE Cart c SET c.user = :user, c.sessionId = null WHERE c.sessionId = :sessionId AND c.isActive = true")
    int transferCartToUser(@Param("sessionId") String sessionId, @Param("user") User user);
    
    // Merge carts when user has both session and user cart
    @Query("SELECT c FROM Cart c WHERE (c.sessionId = :sessionId OR c.user = :user) AND c.isActive = true")
    List<Cart> findCartsForMerging(@Param("sessionId") String sessionId, @Param("user") User user);
    
    // Delete inactive carts older than specified time
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.isActive = false AND c.updatedAt < :cutoffTime")
    int deleteOldInactiveCarts(@Param("cutoffTime") LocalDateTime cutoffTime);
}
