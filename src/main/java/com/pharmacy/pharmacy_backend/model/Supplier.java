package com.pharmacy.pharmacy_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "suppliers")
@SuppressWarnings("unused")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Column(nullable = false)
    private String name;

    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "registered_date")
    private LocalDate registeredDate = LocalDate.now();

    private String notes;

    public Supplier() {}

    @SuppressWarnings("unused")
    public Supplier(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean hasValidContact() {
        return phone != null && !phone.trim().isEmpty();
    }

    public String getDisplayName() {
        return name + " (ID: " + id + ")";
    }

    public Long getId() { return id; }

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

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDate getRegisteredDate() { return registeredDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}