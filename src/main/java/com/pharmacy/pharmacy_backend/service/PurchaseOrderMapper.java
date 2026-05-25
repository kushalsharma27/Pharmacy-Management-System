package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.*;
import com.pharmacy.pharmacy_backend.model.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@SuppressWarnings("unused")
public class PurchaseOrderMapper {

    // Convert PurchaseOrder Entity to PurchaseOrderResponse DTO
    public PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null) {
            return null;
        }

        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(purchaseOrder.getId());
        response.setOrderNumber(purchaseOrder.getOrderNumber());

        // Map supplier info
        if (purchaseOrder.getSupplier() != null) {
            Supplier supplier = purchaseOrder.getSupplier();
            response.setSupplierId(supplier.getId());
            response.setSupplierName(supplier.getName());
            response.setSupplierContactPerson(supplier.getContactPerson());
        }

        response.setOrderDate(purchaseOrder.getOrderDate());
        response.setExpectedDelivery(purchaseOrder.getExpectedDelivery());
        response.setStatus(purchaseOrder.getStatus().name());
        response.setTotalAmount(purchaseOrder.getTotalAmount());
        response.setPaymentMethod(purchaseOrder.getPaymentMethod());
        response.setNotes(purchaseOrder.getNotes());

        // Map items
        if (purchaseOrder.getItems() != null) {
            response.setItems(purchaseOrder.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Convert PurchaseOrderItem Entity to PurchaseOrderItemResponse DTO
    public PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        if (item == null) {
            return null;
        }

        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.setId(item.getId());

        // Map medicine info
        if (item.getMedicine() != null) {
            Medicine medicine = item.getMedicine();
            response.setMedicineId(medicine.getId());
            response.setMedicineName(medicine.getName());
            response.setMedicineGenericName(medicine.getGenericName());

            // Add medicine batch number from the item if available, otherwise from medicine
            if (item.getBatchNumber() != null && !item.getBatchNumber().isEmpty()) {
                response.setBatchNumber(item.getBatchNumber());
            } else {
                response.setBatchNumber(medicine.getBatchNumber());
            }
        }

        response.setQuantity(item.getQuantity());
        response.setPurchasePrice(item.getPurchasePrice());
        response.setReceivedQuantity(item.getReceivedQuantity());
        response.setExpiryDate(item.getExpiryDate());
        response.setLineTotal(item.getLineTotal());
        response.setFullyReceived(item.isFullyReceived());

        return response;
    }

    // Convert PurchaseOrderRequest DTO to PurchaseOrder Entity
    public PurchaseOrder toEntity(PurchaseOrderRequest request, Supplier supplier) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setExpectedDelivery(request.getExpectedDelivery());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setStatus(PurchaseOrderStatus.DRAFT);

        return order;
    }

    // Convert PurchaseOrderItemRequest DTO to PurchaseOrderItem Entity
    public PurchaseOrderItem toItemEntity(PurchaseOrderItemRequest request, PurchaseOrder order, Medicine medicine) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(order);
        item.setMedicine(medicine);
        item.setQuantity(request.getQuantity());
        item.setPurchasePrice(request.getPurchasePrice());
        item.setBatchNumber(request.getBatchNumber());
        item.setExpiryDate(request.getExpiryDate());

        return item;
    }
}