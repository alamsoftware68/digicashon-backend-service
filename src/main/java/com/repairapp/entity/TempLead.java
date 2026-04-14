package com.repairapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "temp_leads", indexes = {
        @Index(name = "idx_temp_lead_mobile", columnList = "mobile"),
        @Index(name = "idx_temp_lead_created", columnList = "created_at")
})
public class TempLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    @Column(name = "super_category", length = 100)
    private String superCategory;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "brand_model_name", length = 100)
    private String brandModelName;

    @Column(name = "processor_name", length = 100)
    private String processorName;

    @Column(name = "ram_label", length = 50)
    private String ramLabel;

    @Column(name = "storage_label", length = 50)
    private String storageLabel;

    @Column(name = "graphics_name", length = 100)
    private String graphicsName;

    @Column(name = "screen_size_label", length = 50)
    private String screenSizeLabel;

    @Column(name = "condition_code", length = 50)
    private String conditionCode;

    @Column(name = "age_code", length = 50)
    private String ageCode;

    @Column(name = "final_price", precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "evaluation_id")
    private Long evaluationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getSuperCategory() { return superCategory; }
    public void setSuperCategory(String superCategory) { this.superCategory = superCategory; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getBrandModelName() { return brandModelName; }
    public void setBrandModelName(String brandModelName) { this.brandModelName = brandModelName; }
    public String getProcessorName() { return processorName; }
    public void setProcessorName(String processorName) { this.processorName = processorName; }
    public String getRamLabel() { return ramLabel; }
    public void setRamLabel(String ramLabel) { this.ramLabel = ramLabel; }
    public String getStorageLabel() { return storageLabel; }
    public void setStorageLabel(String storageLabel) { this.storageLabel = storageLabel; }
    public String getGraphicsName() { return graphicsName; }
    public void setGraphicsName(String graphicsName) { this.graphicsName = graphicsName; }
    public String getScreenSizeLabel() { return screenSizeLabel; }
    public void setScreenSizeLabel(String screenSizeLabel) { this.screenSizeLabel = screenSizeLabel; }
    public String getConditionCode() { return conditionCode; }
    public void setConditionCode(String conditionCode) { this.conditionCode = conditionCode; }
    public String getAgeCode() { return ageCode; }
    public void setAgeCode(String ageCode) { this.ageCode = ageCode; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
