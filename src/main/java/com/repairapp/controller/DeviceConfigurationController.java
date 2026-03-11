package com.repairapp.controller;

import com.repairapp.adminread.entity.*;
import com.repairapp.adminread.repository.*;
import com.repairapp.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for fetching device configuration options from Admin database
 * These APIs are used by the Client Portal to populate device selection forms
 */
@Tag(name = "Device Configuration", description = "Device configuration options for client portal")
@RestController
@RequestMapping("/devices/config")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceConfigurationController {

    @Autowired
    private AdminDeviceProcessorRepository processorRepository;

    @Autowired
    private AdminDeviceGraphicsRepository graphicsRepository;

    @Autowired
    private AdminDeviceRamOptionRepository ramRepository;

    @Autowired
    private AdminDeviceStorageOptionRepository storageRepository;

    @Autowired
    private AdminDeviceScreenSizeRepository screenSizeRepository;

    @Autowired
    private AdminDeviceConditionMultiplierRepository conditionRepository;

    @Autowired
    private AdminDeviceAgeDepreciationRepository ageRepository;

    @Autowired
    private AdminDeviceFunctionalIssueRepository issueRepository;

    @Autowired
    private AdminBrandRepository brandRepository;

    @Autowired
    private AdminBrandModelRepository brandModelRepository;

    @Operation(summary = "Get all enabled processors", description = "Fetch all enabled processor options")
    @GetMapping("/processors")
    public ResponseEntity<ApiResponse<List<AdminDeviceProcessor>>> getProcessors() {
        List<AdminDeviceProcessor> processors = processorRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Processors retrieved successfully", processors));
    }

    @Operation(summary = "Get all enabled graphics cards", description = "Fetch all enabled graphics card options")
    @GetMapping("/graphics")
    public ResponseEntity<ApiResponse<List<AdminDeviceGraphics>>> getGraphics() {
        List<AdminDeviceGraphics> graphics = graphicsRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Graphics cards retrieved successfully", graphics));
    }

    @Operation(summary = "Get all enabled RAM options", description = "Fetch all enabled RAM configuration options")
    @GetMapping("/ram-options")
    public ResponseEntity<ApiResponse<List<AdminDeviceRamOption>>> getRamOptions() {
        List<AdminDeviceRamOption> ramOptions = ramRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("RAM options retrieved successfully", ramOptions));
    }

    @Operation(summary = "Get all enabled storage options", description = "Fetch all enabled storage configuration options")
    @GetMapping("/storage-options")
    public ResponseEntity<ApiResponse<List<AdminDeviceStorageOption>>> getStorageOptions() {
        List<AdminDeviceStorageOption> storageOptions = storageRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Storage options retrieved successfully", storageOptions));
    }

    @Operation(summary = "Get all enabled screen sizes", description = "Fetch all enabled screen size options")
    @GetMapping("/screen-sizes")
    public ResponseEntity<ApiResponse<List<AdminDeviceScreenSize>>> getScreenSizes() {
        List<AdminDeviceScreenSize> screenSizes = screenSizeRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Screen sizes retrieved successfully", screenSizes));
    }

    @Operation(summary = "Get all enabled device conditions", description = "Fetch all enabled device condition options")
    @GetMapping("/conditions")
    public ResponseEntity<ApiResponse<List<AdminDeviceConditionMultiplier>>> getConditions() {
        List<AdminDeviceConditionMultiplier> conditions = conditionRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Conditions retrieved successfully", conditions));
    }

    @Operation(summary = "Get all enabled age depreciation options", description = "Fetch all enabled age depreciation options")
    @GetMapping("/age-options")
    public ResponseEntity<ApiResponse<List<AdminDeviceAgeDepreciation>>> getAgeOptions() {
        List<AdminDeviceAgeDepreciation> ageOptions = ageRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Age options retrieved successfully", ageOptions));
    }

    @Operation(summary = "Get all enabled functional issues", description = "Fetch all enabled functional issue options")
    @GetMapping("/functional-issues")
    public ResponseEntity<ApiResponse<List<AdminDeviceFunctionalIssue>>> getFunctionalIssues() {
        List<AdminDeviceFunctionalIssue> issues = issueRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(ApiResponse.success("Functional issues retrieved successfully", issues));
    }

    @Operation(summary = "Get all enabled brands", description = "Fetch all enabled brands")
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<AdminBrand>>> getBrands(
            @RequestParam(required = false) String type) {
        List<AdminBrand> brands;
        if (type != null && !type.isEmpty()) {
            brands = brandRepository.findByBrandTypeAndEnabledTrue(type);
        } else {
            brands = brandRepository.findByEnabledTrueOrderByNameAsc();
        }
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    @Operation(summary = "Get all enabled brand models for a brand", description = "Fetch all enabled models for a specific brand")
    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<ApiResponse<List<AdminBrandModel>>> getBrandModels(
            @PathVariable Long brandId,
            @RequestParam(required = false) String type) {
        List<AdminBrandModel> models;
        if (type != null && !type.isEmpty()) {
            models = brandModelRepository.findByBrandIdAndBrandTypeAndEnabledTrue(brandId, type);
        } else {
            models = brandModelRepository.findByBrandIdAndEnabledTrue(brandId);
        }
        return ResponseEntity.ok(ApiResponse.success("Brand models retrieved successfully", models));
    }
}
