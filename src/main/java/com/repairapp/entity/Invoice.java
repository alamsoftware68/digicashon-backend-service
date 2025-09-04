package com.repairapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice entity for billing information
 */
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 20, message = "Invoice number must not exceed 20 characters")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 20)
    private String invoiceNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal amount;

    @Column(name = "tax_amount", columnDefinition = "DECIMAL(12,2)")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", columnDefinition = "DECIMAL(12,2)")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private RepairOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    // Constructors
    public Invoice() {}

    public Invoice(String invoiceNumber, RepairOrder order, BigDecimal amount) {
        this.invoiceNumber = invoiceNumber;
        this.order = order;
        this.amount = amount;
        this.dueDate = LocalDateTime.now().plusDays(30); // Default 30 days due date
    }

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public RepairOrder getOrder() {
        return order;
    }

    public void setOrder(RepairOrder order) {
        this.order = order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Calculate total amount including tax and discount
     */
    public BigDecimal getTotalAmount() {
        return amount.add(taxAmount).subtract(discountAmount);
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && 
               status == InvoiceStatus.PENDING && 
               LocalDateTime.now().isAfter(dueDate);
    }

    // Invoice status enum
    public enum InvoiceStatus {
        PENDING("Pending"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
