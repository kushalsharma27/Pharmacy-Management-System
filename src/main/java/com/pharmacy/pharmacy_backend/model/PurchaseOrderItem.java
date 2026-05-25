package com.pharmacy.pharmacy_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @NotNull(message = "Purchase order is required")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    @NotNull(message = "Medicine is required")
    private Medicine medicine;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Column(name = "purchase_price")
    @Min(value = 0, message = "Purchase price cannot be negative")
    private double purchasePrice;

    @Column(name = "received_quantity")
    private int receivedQuantity = 0;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    public PurchaseOrderItem() {}

    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Medicine medicine, int quantity, double purchasePrice) {
        this.purchaseOrder = purchaseOrder;
        this.medicine = medicine;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    public double getLineTotal() {
        return quantity * purchasePrice;
    }

    public boolean isFullyReceived() {
        return receivedQuantity >= quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public java.time.LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }
}