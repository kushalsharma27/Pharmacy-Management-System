package com.pharmacy.pharmacy_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SaleResponse {

    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private List<SaleItemResponse> items;
    private Double subtotal;
    private Double discount;
    private Double tax;
    private Double grandTotal;
    private Double amountPaid;
    private Double changeAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private String cashierName;
    private String notes;

    // Constructors
    public SaleResponse() {}

    public SaleResponse(Long id, String invoiceNumber, Double grandTotal, String status) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.grandTotal = grandTotal;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<SaleItemResponse> getItems() { return items; }
    public void setItems(List<SaleItemResponse> items) { this.items = items; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }

    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    public Double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(Double changeAmount) { this.changeAmount = changeAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}