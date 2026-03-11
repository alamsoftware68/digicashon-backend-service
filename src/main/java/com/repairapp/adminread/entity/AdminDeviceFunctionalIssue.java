package com.repairapp.adminread.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Read-only entity for device functional issues from Admin database
 */
@Entity
@Table(name = "device_functional_issues")
@Immutable
public class AdminDeviceFunctionalIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_code")
    private String issueCode;

    @Column(name = "issue_label")
    private String issueLabel;

    @Column(name = "price_deduction", precision = 10, scale = 2)
    private BigDecimal priceDeduction;

    @Column(name = "severity")
    private String severity;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "enabled")
    private Boolean enabled;

    // Getters only (read-only)
    public Long getId() {
        return id;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public String getIssueLabel() {
        return issueLabel;
    }

    public BigDecimal getPriceDeduction() {
        return priceDeduction;
    }

    public String getSeverity() {
        return severity;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
