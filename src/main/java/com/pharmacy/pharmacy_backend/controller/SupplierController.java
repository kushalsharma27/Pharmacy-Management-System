package com.pharmacy.pharmacy_backend.controller;

import com.pharmacy.pharmacy_backend.dto.PurchaseOrderResponse;
import com.pharmacy.pharmacy_backend.dto.SupplierRequest;
import com.pharmacy.pharmacy_backend.model.Supplier;
import com.pharmacy.pharmacy_backend.service.PurchaseOrderService;
import com.pharmacy.pharmacy_backend.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("isAuthenticated()")
@SuppressWarnings("unused")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // ============ DEFAULT ENDPOINT - ACTIVE SUPPLIERS ONLY ============
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    // ============ ADMIN ONLY - ALL SUPPLIERS (INCLUDING INACTIVE) ============
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    // ============ ADMIN ONLY - INACTIVE SUPPLIERS ONLY ============
    @GetMapping("/inactive")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Supplier>> getInactiveSuppliers() {
        List<Supplier> suppliers = supplierService.getInactiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody SupplierRequest supplierRequest) {
        Supplier supplier = supplierService.createSupplier(supplierRequest);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest supplierRequest) {
        Supplier supplier = supplierService.updateSupplier(id, supplierRequest);
        return ResponseEntity.ok(supplier);
    }

    // ============ HARD DELETE - PERMANENT ============
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Supplier>> searchSuppliers(@RequestParam String name) {
        List<Supplier> suppliers = supplierService.searchSuppliersByName(name);
        return ResponseEntity.ok(suppliers);
    }

    // ============ SOFT DELETE - DEACTIVATE ============
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Supplier> deactivateSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.deactivateSupplier(id);
        return ResponseEntity.ok(supplier);
    }

    // ============ REACTIVATE ============
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Supplier> activateSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.activateSupplier(id);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/{id}/purchase-orders")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<List<PurchaseOrderResponse>> getSupplierPurchaseOrders(@PathVariable Long id) {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersBySupplier(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}/statistics/total-spent")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Double> getTotalSpentBySupplier(@PathVariable Long id) {
        double total = purchaseOrderService.getTotalPurchaseAmountBySupplier(id);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}/statistics/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Map<String, Object>> getSupplierSummary(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersBySupplier(id);
        double totalSpent = purchaseOrderService.getTotalPurchaseAmountBySupplier(id);

        long orderCount = orders.size();
        long receivedOrders = orders.stream()
                .filter(order -> "RECEIVED".equals(order.getStatus()))
                .count();
        long pendingOrders = orders.stream()
                .filter(order -> "PENDING".equals(order.getStatus()) || "DRAFT".equals(order.getStatus()))
                .count();

        Map<String, Object> summary = Map.of(
                "supplierId", supplier.getId(),
                "supplierName", supplier.getName(),
                "isActive", supplier.isActive(),
                "totalOrders", orderCount,
                "totalSpent", totalSpent,
                "receivedOrders", receivedOrders,
                "pendingOrders", pendingOrders,
                "registeredDate", supplier.getRegisteredDate(),
                "averageOrderValue", orderCount > 0 ? totalSpent / orderCount : 0
        );

        return ResponseEntity.ok(summary);
    }

    @PatchMapping("/bulk/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Integer> bulkActivateSuppliers(@RequestBody List<Long> supplierIds) {
        int count = 0;
        for (Long id : supplierIds) {
            try {
                supplierService.activateSupplier(id);
                count++;
            } catch (Exception e) {
            }
        }
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/bulk/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Integer> bulkDeactivateSuppliers(@RequestBody List<Long> supplierIds) {
        int count = 0;
        for (Long id : supplierIds) {
            try {
                supplierService.deactivateSupplier(id);
                count++;
            } catch (Exception e) {
            }
        }
        return ResponseEntity.ok(count);
    }
}