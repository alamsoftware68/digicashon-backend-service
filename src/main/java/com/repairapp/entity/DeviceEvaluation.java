package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a complete device evaluation for pricing
 */
@Entity
@Table(name = "device_evaluations", indexes = {
        @Index(name = "idx_eval_user", columnList = "user_id"),
        @Index(name = "idx_eval_status", columnList = "status"),
        @Index(name = "idx_eval_created", columnList = "created_at")
})
public class DeviceEvaluation extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_device_eval_user"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @NotBlank(message = "Super category is required")
    @Column(name = "super_category", nullable = false, length = 100)
    private String superCategory; // Windows, Mac, Mobile

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 100)
    private String category; // Laptop, Desktop, Tablet

    @NotNull(message = "Brand ID is required")
    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @NotNull(message = "Brand model ID is required")
    @Column(name = "brand_model_id", nullable = false)
    private Long brandModelId;

    @Column(name = "brand_model_name", length = 100)
    private String brandModelName;

    // System Configuration
    @Column(name = "processor_id")
    private Long processorId;

    @Column(name = "processor_name")
    private String processorName;

    @Column(name = "graphics_id")
    private Long graphicsId;

    @Column(name = "graphics_name")
    private String graphicsName;

    @Column(name = "ram_id")
    private Long ramId;

    @Column(name = "ram_label")
    private String ramLabel;

    @Column(name = "storage_id")
    private Long storageId;

    @Column(name = "storage_label")
    private String storageLabel;

    @Column(name = "screen_size_id")
    private Long screenSizeId;

    @Column(name = "screen_size_label")
    private String screenSizeLabel;

    // Device Condition
    @Column(name = "condition_code", length = 50)
    private String conditionCode; // like-new, good, average, below-average

    @Column(name = "has_invoice")
    private Boolean hasInvoice = false;

    @Column(name = "has_box")
    private Boolean hasBox = false;

    // Device Age
    @Column(name = "age_code", length = 50)
    private String ageCode; // lt-1, 1-3, gt-3

    @Column(name = "age_months")
    private Integer ageMonths;

    // Functional Issues
    @OneToMany(mappedBy = "deviceEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"deviceEvaluation"})
    private List<DeviceFunctionalIssueSelection> functionalIssues = new ArrayList<>();

    // Price Breakdown
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "spec_impact", precision = 10, scale = 2)
    private BigDecimal specImpact; // From processor, graphics, RAM, storage, screen

    @Column(name = "condition_multiplier", precision = 5, scale = 2)
    private BigDecimal conditionMultiplier;

    @Column(name = "age_depreciation", precision = 5, scale = 2)
    private BigDecimal ageDepreciation;

    @Column(name = "issue_deductions", precision = 10, scale = 2)
    private BigDecimal issueDeductions;

    @Column(name = "invoice_premium", precision = 10, scale = 2)
    private BigDecimal invoicePremium;

    @Column(name = "box_premium", precision = 10, scale = 2)
    private BigDecimal boxPremium;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee;

    @NotNull(message = "Final price is required")
    @Column(name = "final_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal finalPrice;

    // Evaluation Status
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public DeviceEvaluation() {}

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getBrandModelId() {
        return brandModelId;
    }

    public void setBrandModelId(Long brandModelId) {
        this.brandModelId = brandModelId;
    }

    public String getBrandModelName() {
        return brandModelName;
    }

    public void setBrandModelName(String brandModelName) {
        this.brandModelName = brandModelName;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public Long getGraphicsId() {
        return graphicsId;
    }

    public void setGraphicsId(Long graphicsId) {
        this.graphicsId = graphicsId;
    }

    public String getGraphicsName() {
        return graphicsName;
    }

    public void setGraphicsName(String graphicsName) {
        this.graphicsName = graphicsName;
    }

    public Long getRamId() {
        return ramId;
    }

    public void setRamId(Long ramId) {
        this.ramId = ramId;
    }

    public String getRamLabel() {
        return ramLabel;
    }

    public void setRamLabel(String ramLabel) {
        this.ramLabel = ramLabel;
    }

    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(Long storageId) {
        this.storageId = storageId;
    }

    public String getStorageLabel() {
        return storageLabel;
    }

    public void setStorageLabel(String storageLabel) {
        this.storageLabel = storageLabel;
    }

    public Long getScreenSizeId() {
        return screenSizeId;
    }

    public void setScreenSizeId(Long screenSizeId) {
        this.screenSizeId = screenSizeId;
    }

    public String getScreenSizeLabel() {
        return screenSizeLabel;
    }

    public void setScreenSizeLabel(String screenSizeLabel) {
        this.screenSizeLabel = screenSizeLabel;
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

    public List<DeviceFunctionalIssueSelection> getFunctionalIssues() {
        return functionalIssues;
    }

    public void setFunctionalIssues(List<DeviceFunctionalIssueSelection> functionalIssues) {
        this.functionalIssues = functionalIssues;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getSpecImpact() {
        return specImpact;
    }

    public void setSpecImpact(BigDecimal specImpact) {
        this.specImpact = specImpact;
    }

    public BigDecimal getConditionMultiplier() {
        return conditionMultiplier;
    }

    public void setConditionMultiplier(BigDecimal conditionMultiplier) {
        this.conditionMultiplier = conditionMultiplier;
    }

    public BigDecimal getAgeDepreciation() {
        return ageDepreciation;
    }

    public void setAgeDepreciation(BigDecimal ageDepreciation) {
        this.ageDepreciation = ageDepreciation;
    }

    public BigDecimal getIssueDeductions() {
        return issueDeductions;
    }

    public void setIssueDeductions(BigDecimal issueDeductions) {
        this.issueDeductions = issueDeductions;
    }

    public BigDecimal getInvoicePremium() {
        return invoicePremium;
    }

    public void setInvoicePremium(BigDecimal invoicePremium) {
        this.invoicePremium = invoicePremium;
    }

    public BigDecimal getBoxPremium() {
        return boxPremium;
    }

    public void setBoxPremium(BigDecimal boxPremium) {
        this.boxPremium = boxPremium;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public EvaluationStatus getStatus() {
        return status;
    }

    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public void addFunctionalIssue(DeviceFunctionalIssueSelection issue) {
        functionalIssues.add(issue);
        issue.setDeviceEvaluation(this);
    }

    public void removeFunctionalIssue(DeviceFunctionalIssueSelection issue) {
        functionalIssues.remove(issue);
        issue.setDeviceEvaluation(null);
    }

    // Enum for evaluation status
    public enum EvaluationStatus {
        PENDING,        // Evaluation created, waiting for approval
        APPROVED,       // Price approved by user
        REJECTED,       // User rejected the price
        ORDER_CREATED,  // Order created from this evaluation
        EXPIRED         // Evaluation expired (not used within timeframe)
    }
}
