package com.pharmacy.pharmacy_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String genericName;

    @Column(nullable = false, unique = true)
    private String batchNumber;

    private String manufacturer;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    private Integer reorderLevel = 10;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    private String category;

    private Integer minimumStockLevel = 10;
    private Integer maximumStockLevel = 500;
    private String storageLocation;
    private String supplierName;
    private LocalDate lastRestockedDate;

    public Medicine() {}

    public Medicine(String name, String batchNumber, String manufacturer,
                    Double price, Integer quantity, LocalDate expiryDate) {
        this.name = name;
        this.batchNumber = batchNumber;
        this.manufacturer = manufacturer;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.manufacturingDate = LocalDate.now();
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean willExpireSoon(int days) {
        return expiryDate != null &&
                expiryDate.isBefore(LocalDate.now().plusDays(days)) &&
                !expiryDate.isBefore(LocalDate.now());
    }

    public boolean needsRestocking() {
        return minimumStockLevel != null && quantity <= minimumStockLevel;
    }

    public boolean isOverstocked() {
        return maximumStockLevel != null && quantity > maximumStockLevel;
    }
    public Integer getStockToOrder() {
        if (needsRestocking() && maximumStockLevel != null) {
            return maximumStockLevel - quantity;
        }
        return 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(Integer minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDate getLastRestockedDate() { return lastRestockedDate; }
    public void setLastRestockedDate(LocalDate lastRestockedDate) { this.lastRestockedDate = lastRestockedDate; }

}