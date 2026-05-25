package com.pharmacy.pharmacy_backend.controller;

import com.pharmacy.pharmacy_backend.dto.PurchaseOrderRequest;
import com.pharmacy.pharmacy_backend.dto.PurchaseOrderResponse;
import com.pharmacy.pharmacy_backend.model.PurchaseOrderStatus;
import com.pharmacy.pharmacy_backend.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase-orders")
@PreAuthorize("isAuthenticated()")
@SuppressWarnings("unused")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getAllPurchaseOrderResponses();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        try {
            PurchaseOrderResponse order = purchaseOrderService.getPurchaseOrderResponseById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersBySupplier(@PathVariable Long supplierId) {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersByStatus(@PathVariable String status) {
        try {
            PurchaseOrderStatus statusEnum = PurchaseOrderStatus.valueOf(status.toUpperCase());
            List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersByStatus(statusEnum);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/needing-action")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<List<PurchaseOrderResponse>> getOrdersNeedingAction() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getOrdersNeedingAction();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<List<PurchaseOrderResponse>> getOverduePurchaseOrders() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getOverduePurchaseOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        try {
            PurchaseOrderResponse createdOrder = purchaseOrderService.createPurchaseOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequest request) {
        try {
            PurchaseOrderResponse updatedOrder = purchaseOrderService.updatePurchaseOrderWithResponse(id, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            PurchaseOrderStatus statusEnum = PurchaseOrderStatus.valueOf(status.toUpperCase());
            PurchaseOrderResponse updatedOrder = purchaseOrderService.updateStatusWithResponse(id, statusEnum);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid status. Valid values are: " +
                            Arrays.toString(PurchaseOrderStatus.values()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/items/{itemId}/receive")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<PurchaseOrderResponse> receiveItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) LocalDate expiryDate) {
        try {
            PurchaseOrderResponse updatedOrder = purchaseOrderService.receiveItemWithResponse(
                    orderId, itemId, quantity, batchNumber, expiryDate);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/statistics/total")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Double> getTotalPurchaseAmount() {
        double total = purchaseOrderService.getTotalPurchaseAmount();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/statistics/supplier/{supplierId}/total")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<Double> getTotalPurchaseAmountBySupplier(@PathVariable Long supplierId) {
        double total = purchaseOrderService.getTotalPurchaseAmountBySupplier(supplierId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/status-values")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public List<String> getStatusValues() {
        return Arrays.stream(PurchaseOrderStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}