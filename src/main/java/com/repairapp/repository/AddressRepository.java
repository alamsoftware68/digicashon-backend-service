package com.repairapp.repository;

import com.repairapp.entity.Address;
import com.repairapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find addresses by user
     */
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    /**
     * Find user's default address
     */
    Optional<Address> findByUserAndIsDefault(User user, Boolean isDefault);

    /**
     * Find addresses by user and type
     */
    List<Address> findByUserAndType(User user, String type);

    /**
     * Count addresses by user
     */
    long countByUser(User user);

    /**
     * Find addresses by city
     */
    @Query("SELECT a FROM Address a WHERE LOWER(a.city) = LOWER(:city)")
    List<Address> findByCity(@Param("city") String city);

    /**
     * Find addresses by state
     */
    @Query("SELECT a FROM Address a WHERE LOWER(a.state) = LOWER(:state)")
    List<Address> findByState(@Param("state") String state);
}
