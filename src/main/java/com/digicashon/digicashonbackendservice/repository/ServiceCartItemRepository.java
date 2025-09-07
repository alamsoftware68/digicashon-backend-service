package com.digicashon.digicashonbackendservice.repository;

import com.digicashon.digicashonbackendservice.entity.ServiceCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCartItemRepository extends JpaRepository<ServiceCartItem, Long> {

    /**
     * Find all items in a specific cart
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId")
    List<ServiceCartItem> findByCartId(@Param("cartId") Long cartId);

    /**
     * Find specific item in cart by service item ID
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId AND sci.serviceItem.id = :serviceItemId")
    Optional<ServiceCartItem> findByCartIdAndServiceItemId(@Param("cartId") Long cartId, @Param("serviceItemId") Long serviceItemId);

    /**
     * Check if service item exists in cart
     */
    @Query("SELECT CASE WHEN COUNT(sci) > 0 THEN true ELSE false END FROM ServiceCartItem sci " +
           "WHERE sci.serviceCart.id = :cartId AND sci.serviceItem.id = :serviceItemId")
    boolean existsByCartIdAndServiceItemId(@Param("cartId") Long cartId, @Param("serviceItemId") Long serviceItemId);

    /**
     * Find all items for a specific service item (across all carts)
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceItem.id = :serviceItemId")
    List<ServiceCartItem> findByServiceItemId(@Param("serviceItemId") Long serviceItemId);

    /**
     * Find cart items by category name
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.categoryName = :categoryName")
    List<ServiceCartItem> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Find cart items by service title (partial match)
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceTitle LIKE %:title%")
    List<ServiceCartItem> findByServiceTitleContaining(@Param("title") String title);

    /**
     * Get total quantity of a specific service item across all carts
     */
    @Query("SELECT COALESCE(SUM(sci.quantity), 0) FROM ServiceCartItem sci WHERE sci.serviceItem.id = :serviceItemId")
    int getTotalQuantityByServiceItemId(@Param("serviceItemId") Long serviceItemId);

    /**
     * Get total value of items in a cart
     */
    @Query("SELECT COALESCE(SUM(sci.totalPrice), 0) FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId")
    double getTotalValueByCartId(@Param("cartId") Long cartId);

    /**
     * Count items in a specific cart
     */
    @Query("SELECT COUNT(sci) FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId")
    int countByCartId(@Param("cartId") Long cartId);

    /**
     * Delete all items from a cart
     */
    @Modifying
    @Query("DELETE FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId")
    int deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Delete specific item from cart
     */
    @Modifying
    @Query("DELETE FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId AND sci.serviceItem.id = :serviceItemId")
    int deleteByCartIdAndServiceItemId(@Param("cartId") Long cartId, @Param("serviceItemId") Long serviceItemId);

    /**
     * Update quantity for specific cart item
     */
    @Modifying
    @Query("UPDATE ServiceCartItem sci SET sci.quantity = :quantity, sci.totalPrice = sci.unitPrice * CAST(:quantity AS double) " +
           "WHERE sci.id = :itemId")
    int updateQuantity(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * Find items in cart with service details
     */
    @Query("SELECT sci FROM ServiceCartItem sci JOIN FETCH sci.serviceItem si " +
           "WHERE sci.serviceCart.id = :cartId ORDER BY sci.createdAt ASC")
    List<ServiceCartItem> findByCartIdWithServiceDetails(@Param("cartId") Long cartId);

    /**
     * Find items by cart ID and fetch service category
     */
    @Query("SELECT sci FROM ServiceCartItem sci " +
           "JOIN FETCH sci.serviceItem si " +
           "JOIN FETCH si.serviceCategory sc " +
           "WHERE sci.serviceCart.id = :cartId ORDER BY sci.createdAt ASC")
    List<ServiceCartItem> findByCartIdWithFullDetails(@Param("cartId") Long cartId);

    /**
     * Get cart items grouped by category
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId " +
           "ORDER BY sci.categoryName, sci.createdAt ASC")
    List<ServiceCartItem> findByCartIdGroupedByCategory(@Param("cartId") Long cartId);

    /**
     * Find duplicate items in same cart (same service item)
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId " +
           "AND sci.serviceItem.id = :serviceItemId AND sci.id <> :excludeItemId")
    List<ServiceCartItem> findDuplicateItemsInCart(@Param("cartId") Long cartId, 
                                                  @Param("serviceItemId") Long serviceItemId, 
                                                  @Param("excludeItemId") Long excludeItemId);

    /**
     * Find items with notes
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId AND sci.notes IS NOT NULL")
    List<ServiceCartItem> findByCartIdWithNotes(@Param("cartId") Long cartId);

    /**
     * Find recently added items (last N hours)
     */
    @Query("SELECT sci FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId " +
           "AND sci.createdAt >= CURRENT_TIMESTAMP - :hours HOUR")
    List<ServiceCartItem> findRecentItemsInCart(@Param("cartId") Long cartId, @Param("hours") int hours);

    /**
     * Get summary statistics for a cart
     */
    @Query("SELECT new map(" +
           "COUNT(sci) as itemCount, " +
           "COALESCE(SUM(sci.quantity), 0) as totalQuantity, " +
           "COALESCE(SUM(sci.totalPrice), 0) as totalAmount, " +
           "COUNT(DISTINCT sci.categoryName) as categoryCount) " +
           "FROM ServiceCartItem sci WHERE sci.serviceCart.id = :cartId")
    List<Object> getCartSummary(@Param("cartId") Long cartId);
}
