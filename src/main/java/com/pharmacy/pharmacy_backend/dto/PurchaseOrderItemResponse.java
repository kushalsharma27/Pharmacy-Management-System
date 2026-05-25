package com.pharmacy.pharmacy_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class PurchaseOrderItemResponse {
    private Long id;

    // Medicine info (flattened)
    private Long medicineId;
    private String medicineName;
    private String medicineGenericName;
    private String medicineBrand;

    private int quantity;
    private double purchasePrice;
    private int receivedQuantity;
    private String batchNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    // Calculated field
    private double lineTotal;

    // Status field
    private boolean fullyReceived;

    // Constructors
    public PurchaseOrderItemResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getMedicineGenericName() { return medicineGenericName; }
    public void setMedicineGenericName(String medicineGenericName) { this.medicineGenericName = medicineGenericName; }

    public String getMedicineBrand() { return medicineBrand; }
    public void setMedicineBrand(String medicineBrand) { this.medicineBrand = medicineBrand; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }

    public boolean isFullyReceived() { return fullyReceived; }
    public void setFullyReceived(boolean fullyReceived) { this.fullyReceived = fullyReceived; }
}