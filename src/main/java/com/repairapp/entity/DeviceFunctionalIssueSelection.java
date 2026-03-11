package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Entity representing selected functional issues for a device evaluation
 */
@Entity
@Table(name = "device_functional_issue_selections", indexes = {
        @Index(name = "idx_issue_eval", columnList = "device_evaluation_id")
})
public class DeviceFunctionalIssueSelection extends BaseEntity {

    @NotNull(message = "Device evaluation is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_evaluation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_selection_eval"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "functionalIssues"})
    private DeviceEvaluation deviceEvaluation;

    @NotNull(message = "Issue ID is required")
    @Column(name = "issue_id", nullable = false)
    private Long issueId;

    @Column(name = "issue_code", length = 50)
    private String issueCode;

    @Column(name = "issue_label", length = 200)
    private String issueLabel;

    @NotNull(message = "Price deduction is required")
    @Column(name = "price_deduction", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceDeduction;

    @Column(name = "severity", length = 50)
    private String severity; // MINOR, MODERATE, MAJOR, CRITICAL

    // Constructors
    public DeviceFunctionalIssueSelection() {}

    public DeviceFunctionalIssueSelection(Long issueId, String issueCode, String issueLabel, BigDecimal priceDeduction, String severity) {
        this.issueId = issueId;
        this.issueCode = issueCode;
        this.issueLabel = issueLabel;
        this.priceDeduction = priceDeduction;
        this.severity = severity;
    }

    // Getters and Setters
    public DeviceEvaluation getDeviceEvaluation() {
        return deviceEvaluation;
    }

    public void setDeviceEvaluation(DeviceEvaluation deviceEvaluation) {
        this.deviceEvaluation = deviceEvaluation;
    }

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
