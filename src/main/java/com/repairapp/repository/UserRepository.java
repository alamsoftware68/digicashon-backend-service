package com.repairapp.repository;

import com.repairapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    
    /**
     * Find user by mobile number
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * Find user by mobile or email
     */
    @Query("SELECT u FROM User u WHERE u.phone = :identifier OR u.email = :identifier")
    Optional<User> findByMobileOrEmail(@Param("identifier") String identifier);
    
    /**
     * Check if mobile number exists
     */
    boolean existsByPhone(String phone);


    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     */
    List<User> findByRole(User.Role role);

    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);

    /**
     * Find users by name containing (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) AND u.isActive = true")
    List<User> findByNameContainingIgnoreCaseAndIsActive(@Param("name") String name);

    /**
     * Find technicians available for assignment
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TECHNICIAN' AND u.isActive = true")
    List<User> findAvailableTechnicians();

    /**
     * Count active users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") User.Role role);
}
