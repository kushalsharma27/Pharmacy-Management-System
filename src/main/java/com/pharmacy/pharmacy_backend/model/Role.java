package com.pharmacy.pharmacy_backend.model;

public enum Role {
    ADMIN,
    PHARMACIST,
    CASHIER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}