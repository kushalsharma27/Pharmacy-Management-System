package com.pharmacy.pharmacy_backend.dto;

import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("unused")  // Used by Jackson deserialization
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String address;
    private String gstNumber;
    private String paymentTerms;
    private String notes;

    // DEFAULT CONSTRUCTOR - REQUIRED FOR JACKSON!
    public SupplierRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}