package com.repairapp.dto.deviceeval;

import com.repairapp.entity.DeviceEvaluation;
import com.repairapp.entity.DeviceFunctionalIssueSelection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for device evaluation response with calculated price
 */
public class DeviceEvaluationResponse {

    private Long id;
    private Long userId;
    private String superCategory;
    private String category;
    private Long brandId;
    private String brandName;
    private Long brandModelId;
    private String brandModelName;

    // System Configuration
    private Long processorId;
    private String processorName;
    private Long graphicsId;
    private String graphicsName;
    private Long ramId;
    private String ramLabel;
    private Long storageId;
    private String storageLabel;
    private Long screenSizeId;
    private String screenSizeLabel;

    // Device Condition
    private String conditionCode;
    private Boolean hasInvoice;
    private Boolean hasBox;

    // Device Age
    private String ageCode;
    private Integer ageMonths;

    // Functional Issues
    private List<FunctionalIssueDto> functionalIssues;

    // Price Breakdown
    private PriceBreakdownDto priceBreakdown;

    private BigDecimal finalPrice;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public DeviceEvaluationResponse() {}

    // Static factory method to create from entity
    public static DeviceEvaluationResponse fromEntity(DeviceEvaluation entity) {
        DeviceEvaluationResponse dto = new DeviceEvaluationResponse();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setSuperCategory(entity.getSuperCategory());
        dto.setCategory(entity.getCategory());
        dto.setBrandId(entity.getBrandId());
        dto.setBrandName(entity.getBrandName());
        dto.setBrandModelId(entity.getBrandModelId());
        dto.setBrandModelName(entity.getBrandModelName());

        dto.setProcessorId(entity.getProcessorId());
        dto.setProcessorName(entity.getProcessorName());
        dto.setGraphicsId(entity.getGraphicsId());
        dto.setGraphicsName(entity.getGraphicsName());
        dto.setRamId(entity.getRamId());
        dto.setRamLabel(entity.getRamLabel());
        dto.setStorageId(entity.getStorageId());
        dto.setStorageLabel(entity.getStorageLabel());
        dto.setScreenSizeId(entity.getScreenSizeId());
        dto.setScreenSizeLabel(entity.getScreenSizeLabel());

        dto.setConditionCode(entity.getConditionCode());
        dto.setHasInvoice(entity.getHasInvoice());
        dto.setHasBox(entity.getHasBox());

        dto.setAgeCode(entity.getAgeCode());
        dto.setAgeMonths(entity.getAgeMonths());

        // Map functional issues
        if (entity.getFunctionalIssues() != null) {
            dto.setFunctionalIssues(
                entity.getFunctionalIssues().stream()
                    .map(FunctionalIssueDto::fromEntity)
                    .collect(Collectors.toList())
            );
        }

        // Map price breakdown
        PriceBreakdownDto breakdown = new PriceBreakdownDto();
        breakdown.setBasePrice(entity.getBasePrice());
        breakdown.setSpecImpact(entity.getSpecImpact());
        breakdown.setConditionMultiplier(entity.getConditionMultiplier());
        breakdown.setAgeDepreciation(entity.getAgeDepreciation());
        breakdown.setIssueDeductions(entity.getIssueDeductions());
        breakdown.setInvoicePremium(entity.getInvoicePremium());
        breakdown.setBoxPremium(entity.getBoxPremium());
        breakdown.setProcessingFee(entity.getProcessingFee());
        dto.setPriceBreakdown(breakdown);

        dto.setFinalPrice(entity.getFinalPrice());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<FunctionalIssueDto> getFunctionalIssues() {
        return functionalIssues;
    }

    public void setFunctionalIssues(List<FunctionalIssueDto> functionalIssues) {
        this.functionalIssues = functionalIssues;
    }

    public PriceBreakdownDto getPriceBreakdown() {
        return priceBreakdown;
    }

    public void setPriceBreakdown(PriceBreakdownDto priceBreakdown) {
        this.priceBreakdown = priceBreakdown;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner DTOs
    public static class FunctionalIssueDto {
        private Long issueId;
        private String issueCode;
        private String issueLabel;
        private BigDecimal priceDeduction;
        private String severity;

        public static FunctionalIssueDto fromEntity(DeviceFunctionalIssueSelection entity) {
            FunctionalIssueDto dto = new FunctionalIssueDto();
            dto.setIssueId(entity.getIssueId());
            dto.setIssueCode(entity.getIssueCode());
            dto.setIssueLabel(entity.getIssueLabel());
            dto.setPriceDeduction(entity.getPriceDeduction());
            dto.setSeverity(entity.getSeverity());
            return dto;
        }

        // Getters and Setters
        public Long getIssueId() {
            return issueId;
        }

        public void setIssueId(Long issueId) {
            this.issueId = issueId;
        }

        public String getIssueCode() {
            return issueCode;
        }

        public void setIssueCode(String issueCode) {
            this.issueCode = issueCode;
        }

        public String getIssueLabel() {
            return issueLabel;
        }

        public void setIssueLabel(String issueLabel) {
            this.issueLabel = issueLabel;
        }

        public BigDecimal getPriceDeduction() {
            return priceDeduction;
        }

        public void setPriceDeduction(BigDecimal priceDeduction) {
            this.priceDeduction = priceDeduction;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    public static class PriceBreakdownDto {
        private BigDecimal basePrice;
        private BigDecimal specImpact;
        private BigDecimal conditionMultiplier;
        private BigDecimal ageDepreciation;
        private BigDecimal issueDeductions;
        private BigDecimal invoicePremium;
        private BigDecimal boxPremium;
        private BigDecimal processingFee;

        // Getters and Setters
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
    }
}
