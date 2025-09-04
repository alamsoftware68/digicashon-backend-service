package com.repairapp.service;

import com.repairapp.entity.Address;
import com.repairapp.entity.PaymentMethod;
import com.repairapp.entity.User;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.AddressRepository;
import com.repairapp.repository.PaymentMethodRepository;
import com.repairapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User service for user management operations
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(Long userId, User userDetails) {
        User user = getUserById(userId);
        
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null) {
            // Check if email is already taken by another user
            userRepository.findByEmail(userDetails.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new RuntimeException("Email is already in use by another user");
                        }
                    });
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User profile updated for phone: {}", updatedUser.getPhone());
        
        return updatedUser;
    }

    /**
     * Get user addresses
     */
    public List<Address> getUserAddresses(Long userId) {
        User user = getUserById(userId);
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
    }

    /**
     * Add new address for user
     */
    public Address addUserAddress(Long userId, Address address) {
        User user = getUserById(userId);
        
        // If this is set as default, unset other default addresses
        if (address.getIsDefault()) {
            addressRepository.findByUserAndIsDefault(user, true)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        
        logger.info("New address added for user with phone: {}", user.getPhone());
        return savedAddress;
    }

    /**
     * Update user address
     */
    public Address updateUserAddress(Long userId, Long addressId, Address addressDetails) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Check if address belongs to the user
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the user");
        }

        // If setting as default, unset other default addresses
        if (addressDetails.getIsDefault() && !address.getIsDefault()) {
            addressRepository.findByUserAndIsDefault(user, true)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        // Update address fields
        if (addressDetails.getType() != null) {
            address.setType(addressDetails.getType());
        }
        if (addressDetails.getAddress() != null) {
            address.setAddress(addressDetails.getAddress());
        }
        if (addressDetails.getCity() != null) {
            address.setCity(addressDetails.getCity());
        }
        if (addressDetails.getState() != null) {
            address.setState(addressDetails.getState());
        }
        if (addressDetails.getZipCode() != null) {
            address.setZipCode(addressDetails.getZipCode());
        }
        if (addressDetails.getIsDefault() != null) {
            address.setIsDefault(addressDetails.getIsDefault());
        }

        Address updatedAddress = addressRepository.save(address);
        logger.info("Address updated for user with phone: {}", user.getPhone());
        
        return updatedAddress;
    }

    /**
     * Delete user address
     */
    public void deleteUserAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the user");
        }

        addressRepository.delete(address);
        logger.info("Address deleted for user with phone: {}", user.getPhone());
    }

    /**
     * Get user payment methods
     */
    public List<PaymentMethod> getUserPaymentMethods(Long userId) {
        User user = getUserById(userId);
        return paymentMethodRepository.findByUserAndIsActiveOrderByIsDefaultDescCreatedAtDesc(user, true);
    }

    /**
     * Add payment method for user
     */
    public PaymentMethod addUserPaymentMethod(Long userId, PaymentMethod paymentMethod) {
        User user = getUserById(userId);
        
        // If this is set as default, unset other default payment methods
        if (paymentMethod.getIsDefault()) {
            paymentMethodRepository.findByUserAndIsDefaultAndIsActive(user, true, true)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        paymentMethodRepository.save(existingDefault);
                    });
        }

        paymentMethod.setUser(user);
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        
        logger.info("New payment method added for user with phone: {}", user.getPhone());
        return savedPaymentMethod;
    }

    /**
     * Delete payment method
     */
    public void deleteUserPaymentMethod(Long userId, Long paymentMethodId) {
        User user = getUserById(userId);
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId));

        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new RuntimeException("Payment method does not belong to the user");
        }

        // Soft delete by setting active to false
        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
        
        logger.info("Payment method deleted for user with phone: {}", user.getPhone());
    }

    /**
     * Get all technicians
     */
    public List<User> getAllTechnicians() {
        return userRepository.findByRoleAndIsActive(User.Role.TECHNICIAN, true);
    }

    /**
     * Get available technicians
     */
    public List<User> getAvailableTechnicians() {
        return userRepository.findAvailableTechnicians();
    }
    
    /**
     * Set default address for user
     */
    public Address setDefaultAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the user");
        }
        
        // Unset current default address
        addressRepository.findByUserAndIsDefault(user, true)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    addressRepository.save(existingDefault);
                });
        
        // Set new default
        address.setIsDefault(true);
        Address savedAddress = addressRepository.save(address);
        
        logger.info("Default address set for user with phone: {}", user.getPhone());
        return savedAddress;
    }
    
    /**
     * Get default address for user
     */
    public Address getDefaultAddress(Long userId) {
        User user = getUserById(userId);
        return addressRepository.findByUserAndIsDefault(user, true).orElse(null);
    }
}
