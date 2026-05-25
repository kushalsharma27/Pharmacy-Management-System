package com.pharmacy.pharmacy_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Supplier is required")
    private Supplier supplier;

    @Column(name = "order_date")
    private LocalDate orderDate = LocalDate.now();

    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(name = "total_amount")
    private double totalAmount = 0.0;

    @Column(name = "payment_method")
    private String paymentMethod;

    private String notes;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @PostPersist  // ✅ FIXED: Runs AFTER ID is generated
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            String year = String.valueOf(LocalDate.now().getYear());
            String sequence = String.format("%05d", this.id);  // ✅ ID is now available!
            this.orderNumber = "PO-" + year + "-" + sequence;
        }
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPurchasePrice())
                .sum();
    }

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        calculateTotal();
    }

    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        calculateTotal();
    }

    public PurchaseOrder() {}

    public Long getId() { return id; }

    public String getOrderNumber() { return orderNumber; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }

    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }

    /**
     * @deprecated Use calculateTotal() instead - this field is auto-calculated
     */
    @Deprecated
    public double getTotalAmount() { return totalAmount; }

    // ❌ setTotalAmount() removed - Should be calculated, not set manually

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PurchaseOrderItem> getItems() { return items; }

    /**
     * Sets items and recalculates total
     */
    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items;
        calculateTotal();  // ✅ Auto-update total
    }
}