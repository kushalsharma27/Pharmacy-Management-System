package com.pharmacy.pharmacy_backend.model;

public enum PurchaseOrderStatus {
    PENDING,
    DRAFT,           // Order being created
    APPROVED,        // Approved by admin/pharmacist
    ORDERED,         // Sent to supplier
    DELIVERED,       // Goods delivered
    RECEIVED,        // Goods received and verified
    CANCELLED,       // Order cancelled
    PARTIALLY_RECEIVED  // Partial delivery received
}