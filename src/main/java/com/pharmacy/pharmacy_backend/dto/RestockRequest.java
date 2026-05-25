package com.pharmacy.pharmacy_backend.dto;

public class RestockRequest {
    private Integer quantity;
    private String supplier;

    public RestockRequest() {}

    @SuppressWarnings("unused")
    public RestockRequest(Integer quantity, String supplier) {
        this.quantity = quantity;
        this.supplier = supplier;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}