package com.repairapp.repository;

import com.repairapp.entity.Cart;
import com.repairapp.entity.CartItem;
import com.repairapp.entity.RepairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find all items in a cart
    List<CartItem> findByCart(Cart cart);
    
    // Find all items in a cart ordered by creation time
    List<CartItem> findByCartOrderByCreatedAtAsc(Cart cart);
    
    // Find specific item in cart by service
    Optional<CartItem> findByCartAndRepairService(Cart cart, RepairService repairService);
    
    // Find items by cart ID
    List<CartItem> findByCartId(Long cartId);
    
    // Count items in a cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart = :cart")
    long countByCart(@Param("cart") Cart cart);
    
    // Get total quantity of all items in a cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart = :cart")
    Integer getTotalQuantityInCart(@Param("cart") Cart cart);
    
    // Get total price of all items in a cart
    @Query("SELECT COALESCE(SUM(ci.totalPrice), 0.0) FROM CartItem ci WHERE ci.cart = :cart")
    Double getTotalPriceInCart(@Param("cart") Cart cart);
    
    // Find items by repair service
    List<CartItem> findByRepairService(RepairService repairService);
    
    // Find items by repair service ID
    List<CartItem> findByRepairServiceId(Long serviceId);
    
    // Find items in cart with quantity greater than specified value
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.quantity > :minQuantity")
    List<CartItem> findItemsWithMinQuantity(@Param("cart") Cart cart, @Param("minQuantity") Integer minQuantity);
    
    // Find items added recently (within specified hours)
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.createdAt >= :since ORDER BY ci.createdAt DESC")
    List<CartItem> findRecentItemsInCart(@Param("cart") Cart cart, @Param("since") LocalDateTime since);
    
    // Delete all items from a cart
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    int deleteAllByCart(@Param("cart") Cart cart);
    
    // Update quantity for a cart item
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity, ci.totalPrice = :totalPrice, ci.updatedAt = :updatedAt WHERE ci.id = :itemId")
    int updateQuantityAndPrice(@Param("itemId") Long itemId, @Param("quantity") Integer quantity, 
                               @Param("totalPrice") Double totalPrice, @Param("updatedAt") LocalDateTime updatedAt);
    
    // Find items with specific notes pattern
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.notes LIKE :notesPattern")
    List<CartItem> findItemsWithNotesPattern(@Param("cart") Cart cart, @Param("notesPattern") String notesPattern);
    
    // Find items by price range in a cart
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.totalPrice BETWEEN :minPrice AND :maxPrice")
    List<CartItem> findItemsByPriceRange(@Param("cart") Cart cart, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Get most popular services (by cart item count)
    @Query("SELECT ci.repairService, COUNT(ci) as itemCount FROM CartItem ci GROUP BY ci.repairService ORDER BY itemCount DESC")
    List<Object[]> getMostPopularServices();
    
    // Find items added on a specific date
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND DATE(ci.createdAt) = DATE(:targetDate)")
    List<CartItem> findItemsAddedOnDate(@Param("cart") Cart cart, @Param("targetDate") LocalDateTime targetDate);
    
    // Check if service exists in cart
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.cart = :cart AND ci.repairService = :service")
    boolean existsByCartAndRepairService(@Param("cart") Cart cart, @Param("service") RepairService service);
    
    // Find duplicate items (same service in same cart)
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.repairService = :service")
    List<CartItem> findDuplicateItems(@Param("cart") Cart cart, @Param("service") RepairService service);
    
    // Bulk delete items by IDs
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id IN :itemIds")
    int deleteByIdIn(@Param("itemIds") List<Long> itemIds);
    
    // Find items with zero quantity (for cleanup)
    @Query("SELECT ci FROM CartItem ci WHERE ci.quantity <= 0")
    List<CartItem> findItemsWithZeroQuantity();
    
    // Delete items with zero quantity
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.quantity <= 0")
    int deleteItemsWithZeroQuantity();
}
