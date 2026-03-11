package com.repairapp.dto.deviceeval;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for creating a device evaluation and calculating price
 */
public class DeviceEvaluationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Super category is required")
    private String superCategory;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotNull(message = "Brand model ID is required")
    private Long brandModelId;

    // System Configuration
    @NotNull(message = "Processor ID is required")
    private Long processorId;

    private Long graphicsId;

    @NotNull(message = "RAM ID is required")
    private Long ramId;

    @NotNull(message = "Storage ID is required")
    private Long storageId;

    private Long screenSizeId;

    // Device Condition
    @NotBlank(message = "Condition code is required")
    private String conditionCode;

    private Boolean hasInvoice = false;

    private Boolean hasBox = false;

    // Device Age
    @NotBlank(message = "Age code is required")
    private String ageCode;

    private Integer ageMonths;

    // Functional Issues (list of issue IDs)
    private List<Long> functionalIssueIds;

    private String notes;

    // Constructors
    public DeviceEvaluationRequest() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSuperCategory() {
        return superCategory;
    }

    public void setSuperCategory(String superCategory) {
        this.superCategory = superCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getBrandModelId() {
        return brandModelId;
    }

    public void setBrandModelId(Long brandModelId) {
        this.brandModelId = brandModelId;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public Long getGraphicsId() {
        return graphicsId;
    }

    public void setGraphicsId(Long graphicsId) {
        this.graphicsId = graphicsId;
    }

    public Long getRamId() {
        return ramId;
    }

    public void setRamId(Long ramId) {
        this.ramId = ramId;
    }

    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(Long storageId) {
        this.storageId = storageId;
    }

    public Long getScreenSizeId() {
        return screenSizeId;
    }

    public void setScreenSizeId(Long screenSizeId) {
        this.screenSizeId = screenSizeId;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public Boolean getHasInvoice() {
        return hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public Boolean getHasBox() {
        return hasBox;
    }

    public void setHasBox(Boolean hasBox) {
        this.hasBox = hasBox;
    }

    public String getAgeCode() {
        return ageCode;
    }

    public void setAgeCode(String ageCode) {
        this.ageCode = ageCode;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public List<Long> getFunctionalIssueIds() {
        return functionalIssueIds;
    }

    public void setFunctionalIssueIds(List<Long> functionalIssueIds) {
        this.functionalIssueIds = functionalIssueIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
