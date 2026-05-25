package com.pharmacy.pharmacy_backend.controller;

import com.pharmacy.pharmacy_backend.dto.AdjustStockRequest;
import com.pharmacy.pharmacy_backend.dto.InventoryResponse;
import com.pharmacy.pharmacy_backend.dto.ReceiveItemRequest;
import com.pharmacy.pharmacy_backend.dto.RestockRequest;
import com.pharmacy.pharmacy_backend.model.Medicine;
import com.pharmacy.pharmacy_backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("isAuthenticated()")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        Map<String, Object> summary = inventoryService.getInventorySummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/needs-restock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Medicine>> getMedicinesNeedingRestock() {
        List<Medicine> medicines = inventoryService.getMedicinesNeedingRestock();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Medicine>> getExpiredMedicines() {
        List<Medicine> medicines = inventoryService.getExpiredMedicines();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Medicine>> getMedicinesExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        List<Medicine> medicines = inventoryService.getMedicinesExpiringSoon(days);
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/overstocked")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<Medicine>> getOverstockedMedicines() {
        List<Medicine> medicines = inventoryService.getOverstockedMedicines();
        return ResponseEntity.ok(medicines);
    }

    @PostMapping("/restock/{medicineId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Medicine> restockMedicine(
            @PathVariable Long medicineId,
            @RequestBody RestockRequest request) {
        Medicine medicine = inventoryService.restockMedicine(
                medicineId,
                request.getQuantity(),
                request.getSupplier()
        );
        return ResponseEntity.ok(medicine);
    }

    @PostMapping("/adjust/{medicineId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Medicine> adjustStock(
            @PathVariable Long medicineId,
            @RequestBody AdjustStockRequest request) {
        Medicine medicine = inventoryService.adjustStock(
                medicineId,
                request.getAdjustment(),
                request.getReason()
        );
        return ResponseEntity.ok(medicine);
    }

    @PostMapping("/receive")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<InventoryResponse> receiveInventory(@Valid @RequestBody ReceiveItemRequest request) {
        InventoryResponse response = inventoryService.receiveItemWithResponse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByMedicine(@PathVariable Long medicineId) {
        List<InventoryResponse> inventory = inventoryService.getInventoryByMedicine(medicineId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/batch/{batchNumber}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByBatchNumber(@PathVariable String batchNumber) {
        List<InventoryResponse> inventory = inventoryService.getInventoryByBatchNumber(batchNumber);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/batches/expired")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getExpiredInventory() {
        List<InventoryResponse> inventory = inventoryService.getExpiredInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/batches/expiring-soon")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getExpiringSoonInventory(
            @RequestParam(defaultValue = "30") int days) {
        List<InventoryResponse> inventory = inventoryService.getExpiringSoonInventory(days);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/batches/low-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getLowStockInventory(
            @RequestParam(defaultValue = "10") int threshold) {
        List<InventoryResponse> inventory = inventoryService.getLowStockInventory(threshold);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/location/{location}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByLocation(@PathVariable String location) {
        List<InventoryResponse> inventory = inventoryService.getInventoryByLocation(location);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/batches/{id}/location")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<InventoryResponse> updateInventoryLocation(
            @PathVariable Long id,
            @RequestParam String location) {
        InventoryResponse inventory = inventoryService.updateInventoryLocation(id, location);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/batches/{id}/quantity")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<InventoryResponse> adjustBatchQuantity(
            @PathVariable Long id,
            @RequestParam Integer adjustment) {
        InventoryResponse inventory = inventoryService.adjustBatchQuantity(id, adjustment);
        return ResponseEntity.ok(inventory);
    }

    @DeleteMapping("/batches/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}