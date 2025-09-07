package com.repairapp.service.impl;

import com.repairapp.dto.address.AddAddressRequest;
import com.repairapp.dto.address.AddressDto;
import com.repairapp.dto.address.EditAddressRequest;
import com.repairapp.entity.Address;
import com.repairapp.entity.User;
import com.repairapp.repository.AddressRepository;
import com.repairapp.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of Address Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    
    @Override
    public AddressDto addAddress(User user, AddAddressRequest request) {
        log.info("Adding new address for user: {}", user.getId());
        
        // If this is set as default, unset any existing default address
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultAddress(user);
        }
        
        // Create new address
        Address address = request.toEntity();
        address.setUser(user);
        
        // Save the address
        Address savedAddress = addressRepository.save(address);
        log.info("Successfully added address with ID: {} for user: {}", savedAddress.getId(), user.getId());
        
        return AddressDto.fromEntity(savedAddress);
    }
    
    @Override
    public AddressDto editAddress(User user, Long addressId, EditAddressRequest request) {
        log.info("Editing address: {} for user: {}", addressId, user.getId());
        
        // Find the address
        Address address = findUserAddress(user, addressId);
        
        // Update the fields
        address.setType(request.getType());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        
        // Save the updated address
        Address updatedAddress = addressRepository.save(address);
        log.info("Successfully updated address: {} for user: {}", addressId, user.getId());
        
        return AddressDto.fromEntity(updatedAddress);
    }
    
    @Override
    public AddressDto setDefaultAddress(User user, Long addressId) {
        log.info("Setting default address: {} for user: {}", addressId, user.getId());
        
        // First unset any existing default address
        unsetDefaultAddress(user);
        
        // Find and set the new default address
        Address address = findUserAddress(user, addressId);
        address.setIsDefault(true);
        
        Address updatedAddress = addressRepository.save(address);
        log.info("Successfully set address: {} as default for user: {}", addressId, user.getId());
        
        return AddressDto.fromEntity(updatedAddress);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(User user) {
        log.info("Fetching addresses for user: {}", user.getId());
        
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        
        return addresses.stream()
                .map(AddressDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AddressDto getDefaultAddress(User user) {
        log.info("Fetching default address for user: {}", user.getId());
        
        Optional<Address> defaultAddress = addressRepository.findByUserAndIsDefault(user, true);
        
        return defaultAddress.map(AddressDto::fromEntity).orElse(null);
    }
    
    @Override
    public void deleteAddress(User user, Long addressId) {
        log.info("Deleting address: {} for user: {}", addressId, user.getId());
        
        Address address = findUserAddress(user, addressId);
        addressRepository.delete(address);
        
        log.info("Successfully deleted address: {} for user: {}", addressId, user.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddress(User user, Long addressId) {
        log.info("Fetching address: {} for user: {}", addressId, user.getId());
        
        Address address = findUserAddress(user, addressId);
        return AddressDto.fromEntity(address);
    }
    
    // Helper methods
    
    /**
     * Find user's address by ID or throw exception
     */
    private Address findUserAddress(User user, Long addressId) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Address not found or doesn't belong to user"));
    }
    
    /**
     * Unset any existing default address for the user
     */
    private void unsetDefaultAddress(User user) {
        Optional<Address> currentDefault = addressRepository.findByUserAndIsDefault(user, true);
        if (currentDefault.isPresent()) {
            Address defaultAddress = currentDefault.get();
            defaultAddress.setIsDefault(false);
            addressRepository.save(defaultAddress);
            log.info("Unset previous default address: {} for user: {}", defaultAddress.getId(), user.getId());
        }
    }
}
