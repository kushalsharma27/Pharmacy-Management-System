package com.pharmacy.pharmacy_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SaleRequest {

    private Long customerId;

    @NotNull(message = "Sale items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<SaleItemRequest> items;

    private Double discount = 0.0;

    // New: Support for multiple payment methods
    private List<PaymentDetails> payments;

    private String notes;

    // For backward compatibility (simple single payment)
    private String paymentMethod;
    private Double amountPaid;

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public List<PaymentDetails> getPayments() { return payments; }
    public void setPayments(List<PaymentDetails> payments) { this.payments = payments; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Backward compatibility methods
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
}