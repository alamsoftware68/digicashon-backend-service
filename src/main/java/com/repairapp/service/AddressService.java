package com.repairapp.service;

import com.repairapp.dto.address.AddAddressRequest;
import com.repairapp.dto.address.AddressDto;
import com.repairapp.dto.address.EditAddressRequest;
import com.repairapp.entity.Address;
import com.repairapp.entity.User;

import java.util.List;

/**
 * Service interface for Address management
 */
public interface AddressService {
    
    /**
     * Add a new address for the user
     * @param user The user
     * @param request Address details
     * @return Created address DTO
     */
    AddressDto addAddress(User user, AddAddressRequest request);
    
    /**
     * Edit an existing address
     * @param user The user
     * @param addressId Address ID to edit
     * @param request Updated address details
     * @return Updated address DTO
     */
    AddressDto editAddress(User user, Long addressId, EditAddressRequest request);
    
    /**
     * Set an address as default
     * @param user The user
     * @param addressId Address ID to set as default
     * @return Updated address DTO
     */
    AddressDto setDefaultAddress(User user, Long addressId);
    
    /**
     * Get all addresses for a user
     * @param user The user
     * @return List of user's addresses
     */
    List<AddressDto> getUserAddresses(User user);
    
    /**
     * Get user's default address
     * @param user The user
     * @return Default address DTO or null
     */
    AddressDto getDefaultAddress(User user);
    
    /**
     * Delete an address
     * @param user The user
     * @param addressId Address ID to delete
     */
    void deleteAddress(User user, Long addressId);
    
    /**
     * Get address by ID for the user
     * @param user The user
     * @param addressId Address ID
     * @return Address DTO
     */
    AddressDto getAddress(User user, Long addressId);
}
