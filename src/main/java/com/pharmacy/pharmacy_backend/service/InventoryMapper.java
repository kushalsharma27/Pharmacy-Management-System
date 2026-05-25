package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.InventoryResponse;
import com.pharmacy.pharmacy_backend.model.Inventory;
import com.pharmacy.pharmacy_backend.model.Medicine;
import com.pharmacy.pharmacy_backend.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryMapper {

    @Autowired
    private SupplierRepository supplierRepository;

    public InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();

        response.setId(inventory.getId());
        response.setBatchNumber(inventory.getBatchNumber());
        response.setQuantity(inventory.getQuantity());
        response.setExpiryDate(inventory.getExpiryDate());
        response.setReceivedDate(inventory.getReceivedDate());
        response.setStorageLocation(inventory.getStorageLocation());
        response.setPurchaseOrderId(inventory.getPurchaseOrderId());

        Medicine medicine = inventory.getMedicine();
        if (medicine != null) {
            response.setMedicineId(medicine.getId());
            response.setMedicineName(medicine.getName());
        }

        if (inventory.getSupplierId() != null) {
            response.setSupplierId(inventory.getSupplierId());
            supplierRepository.findById(inventory.getSupplierId())
                    .ifPresent(supplier -> response.setSupplierName(supplier.getName()));
        }
        response.setExpired(inventory.isExpired());
        response.setDaysUntilExpiry(inventory.getDaysUntilExpiry());

        return response;
    }
}