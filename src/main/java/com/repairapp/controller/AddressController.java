package com.repairapp.controller;

import com.repairapp.dto.address.AddAddressRequest;
import com.repairapp.dto.address.AddressDto;
import com.repairapp.dto.address.EditAddressRequest;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.User;
import com.repairapp.security.UserPrincipal;
import com.repairapp.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Address management in user profile
 */
@Tag(name = "Profile Address Management", description = "APIs for managing user addresses in profile")
@RestController
@RequestMapping("/profile/addresses")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    // ============== GET ADDRESSES ==============

    /**
     * Get all addresses for the authenticated user
     */
    @Operation(summary = "Get user addresses", description = "Retrieve all addresses for the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDto>>> getUserAddresses(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        User user = createUserFromPrincipal(userPrincipal);
        List<AddressDto> addresses = addressService.getUserAddresses(user);
        
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    /**
     * Get default address for the authenticated user
     */
    @Operation(summary = "Get default address", description = "Retrieve the default address for the authenticated user")
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressDto>> getDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        User user = createUserFromPrincipal(userPrincipal);
        AddressDto defaultAddress = addressService.getDefaultAddress(user);
        
        if (defaultAddress == null) {
            return ResponseEntity.ok(ApiResponse.success("No default address found", null));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Default address retrieved successfully", defaultAddress));
    }

    /**
     * Get specific address by ID
     */
    @Operation(summary = "Get address by ID", description = "Retrieve a specific address by ID for the authenticated user")
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressDto>> getAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        try {
            User user = createUserFromPrincipal(userPrincipal);
            AddressDto address = addressService.getAddress(user, addressId);
            
            return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
        } catch (Exception e) {
            log.error("Failed to get address {} for user {}", addressId, userPrincipal.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve address: " + e.getMessage()));
        }
    }

    // ============== ADD ADDRESS ==============

    /**
     * Add a new address for the authenticated user
     */
    @Operation(summary = "Add new address", description = "Add a new address for the authenticated user")
    @PostMapping
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @Valid @RequestBody AddAddressRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        try {
            User user = createUserFromPrincipal(userPrincipal);
            AddressDto newAddress = addressService.addAddress(user, request);
            
            return ResponseEntity.ok(ApiResponse.success("Address added successfully", newAddress));
        } catch (Exception e) {
            log.error("Failed to add address for user {}", userPrincipal.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add address: " + e.getMessage()));
        }
    }

    // ============== EDIT ADDRESS ==============

    /**
     * Edit an existing address
     */
    @Operation(summary = "Edit address", description = "Edit an existing address for the authenticated user")
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressDto>> editAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody EditAddressRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        try {
            User user = createUserFromPrincipal(userPrincipal);
            AddressDto updatedAddress = addressService.editAddress(user, addressId, request);
            
            return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updatedAddress));
        } catch (Exception e) {
            log.error("Failed to edit address {} for user {}", addressId, userPrincipal.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update address: " + e.getMessage()));
        }
    }

    // ============== SET DEFAULT ADDRESS ==============

    /**
     * Set an address as default
     */
    @Operation(summary = "Set default address", description = "Set an existing address as the default address")
    @PutMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressDto>> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        try {
            User user = createUserFromPrincipal(userPrincipal);
            AddressDto defaultAddress = addressService.setDefaultAddress(user, addressId);
            
            return ResponseEntity.ok(ApiResponse.success("Address set as default successfully", defaultAddress));
        } catch (Exception e) {
            log.error("Failed to set default address {} for user {}", addressId, userPrincipal.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to set default address: " + e.getMessage()));
        }
    }

    // ============== DELETE ADDRESS ==============

    /**
     * Delete an address
     */
    @Operation(summary = "Delete address", description = "Delete an existing address")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        try {
            User user = createUserFromPrincipal(userPrincipal);
            addressService.deleteAddress(user, addressId);
            
            return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", "Address removed"));
        } catch (Exception e) {
            log.error("Failed to delete address {} for user {}", addressId, userPrincipal.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete address: " + e.getMessage()));
        }
    }

    // ============== HELPER METHODS ==============

    /**
     * Create User entity from UserPrincipal
     */
    private User createUserFromPrincipal(UserPrincipal userPrincipal) {
        User user = new User();
        user.setId(userPrincipal.getId());
        user.setPhone(userPrincipal.getPhone());
        user.setEmail(userPrincipal.getEmail());
        user.setName(userPrincipal.getName());
        user.setRole(userPrincipal.getRole());
        user.setIsActive(userPrincipal.getIsActive());
        // Set version to avoid optimistic locking issues
        user.setVersion(0L);
        return user;
    }
}
