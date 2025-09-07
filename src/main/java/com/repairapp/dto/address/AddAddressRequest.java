package com.repairapp.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding a new address
 */
public class AddAddressRequest {

    @NotBlank(message = "Address type is required")
    @Pattern(regexp = "^(Home|Office|Others)$", message = "Address type must be Home, Office, or Others")
    private String type;

    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "ZIP code must be exactly 6 digits")
    private String zipCode;

    private Boolean isDefault = false;

    // Constructors
    public AddAddressRequest() {}

    public AddAddressRequest(String type, String address, String city, String state, String zipCode, Boolean isDefault) {
        this.type = type;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Convert to Address entity
     */
    public com.repairapp.entity.Address toEntity() {
        return new com.repairapp.entity.Address(type, address, city, state, zipCode, isDefault);
    }
}
