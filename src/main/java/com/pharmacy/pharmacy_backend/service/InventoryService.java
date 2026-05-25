package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.InventoryResponse;
import com.pharmacy.pharmacy_backend.dto.ReceiveItemRequest;
import com.pharmacy.pharmacy_backend.model.Inventory;
import com.pharmacy.pharmacy_backend.model.Medicine;
import com.pharmacy.pharmacy_backend.repository.InventoryRepository;
import com.pharmacy.pharmacy_backend.repository.MedicineRepository;
import com.pharmacy.pharmacy_backend.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryMapper inventoryMapper;

    public List<Medicine> getMedicinesNeedingRestock() {
        return medicineService.getAllMedicines().stream()
                .filter(Medicine::needsRestocking)
                .toList();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineService.getExpiredMedicines();
    }

    public List<Medicine> getMedicinesExpiringSoon(int days) {
        return medicineService.getExpiringSoonMedicines(days);
    }

    public List<Medicine> getOverstockedMedicines() {
        return medicineService.getAllMedicines().stream()
                .filter(Medicine::isOverstocked)
                .toList();
    }

    @Transactional
    public Medicine restockMedicine(Long medicineId, Integer quantity, String supplier) {
        Medicine medicine = medicineService.getMedicineById(medicineId);

        medicine.setQuantity(medicine.getQuantity() + quantity);
        medicine.setLastRestockedDate(LocalDate.now());
        if (supplier != null) {
            medicine.setSupplierName(supplier);
        }

        return medicineService.updateMedicine(medicineId, medicine);
    }

    @Transactional
    public Medicine adjustStock(Long medicineId, Integer adjustment, String reason) {
        Medicine medicine = medicineService.getMedicineById(medicineId);

        int newQuantity = medicine.getQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new RuntimeException("Cannot adjust stock below 0");
        }

        medicine.setQuantity(newQuantity);
        return medicineService.updateMedicine(medicineId, medicine);
    }

    public Map<String, Object> getInventorySummary() {
        List<Medicine> allMedicines = medicineService.getAllMedicines();

        int totalItems = allMedicines.stream()
                .mapToInt(Medicine::getQuantity)
                .sum();

        double totalValue = allMedicines.stream()
                .mapToDouble(m -> m.getQuantity() * m.getPrice())
                .sum();

        long lowStockCount = allMedicines.stream()
                .filter(medicine -> {
                    Integer minStock = medicine.getMinimumStockLevel();
                    return minStock != null && medicine.getQuantity() <= minStock;
                })
                .count();

        long expiredCount = allMedicines.stream()
                .filter(Medicine::isExpired)
                .count();

        long totalBatches = inventoryRepository.count();

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        long expiringSoonCount = inventoryRepository.findByExpiryDateBetween(now, thirtyDaysLater).size();

        return Map.of(
                "totalItems", totalItems,
                "totalValue", totalValue,
                "totalMedicines", allMedicines.size(),
                "totalBatches", totalBatches,
                "lowStockCount", lowStockCount,
                "expiredCount", expiredCount,
                "expiringSoonCount", expiringSoonCount,
                "lastUpdated", LocalDate.now()
        );
    }

    /**
     * Receive new inventory batch
     */
    @Transactional
    public Inventory receiveItem(ReceiveItemRequest request) {
        // Validate medicine
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + request.getMedicineId()));

        if (request.getSupplierId() != null) {
            supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        }

        if (!inventoryRepository.findByBatchNumber(request.getBatchNumber()).isEmpty()) {
            throw new RuntimeException("Batch number already exists: " + request.getBatchNumber());
        }

        Inventory inventory = new Inventory();
        inventory.setMedicine(medicine);
        inventory.setBatchNumber(request.getBatchNumber());
        inventory.setQuantity(request.getQuantity());
        inventory.setExpiryDate(request.getExpiryDate());
        inventory.setReceivedDate(LocalDate.now());
        inventory.setSupplierId(request.getSupplierId());
        inventory.setPurchaseOrderId(request.getPurchaseOrderId());
        inventory.setStorageLocation(request.getStorageLocation());


        medicine.setQuantity(medicine.getQuantity() + request.getQuantity());
        medicine.setLastRestockedDate(LocalDate.now());
        if (request.getSupplierId() != null) {
            supplierRepository.findById(request.getSupplierId()).ifPresent(supplier ->
                    medicine.setSupplierName(supplier.getName())
            );
        }
        medicineRepository.save(medicine);

        return inventoryRepository.save(inventory);
    }

    /**
     * Receive item with response DTO
     */
    @Transactional
    public InventoryResponse receiveItemWithResponse(ReceiveItemRequest request) {
        Inventory inventory = receiveItem(request);
        return inventoryMapper.toResponse(inventory);
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));
        return inventoryMapper.toResponse(inventory);
    }

    public List<InventoryResponse> getInventoryByMedicine(Long medicineId) {
        return inventoryRepository.findByMedicineId(medicineId).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getInventoryByBatchNumber(String batchNumber) {
        return inventoryRepository.findByBatchNumber(batchNumber).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getExpiredInventory() {
        return inventoryRepository.findByExpiryDateBefore(LocalDate.now()).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getExpiringSoonInventory(int days) {
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusDays(days);
        return inventoryRepository.findByExpiryDateBetween(now, threshold).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getLowStockInventory(int threshold) {
        return inventoryRepository.findLowStockItems(threshold).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getInventoryByLocation(String location) {
        return inventoryRepository.findByStorageLocation(location).stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventory not found with id: " + id);
        }
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public InventoryResponse updateInventoryLocation(Long id, String location) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));
        inventory.setStorageLocation(location);
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse adjustBatchQuantity(Long id, Integer adjustment) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));

        int newQuantity = inventory.getQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new RuntimeException("Cannot adjust quantity below 0");
        }

        inventory.setQuantity(newQuantity);

        Medicine medicine = inventory.getMedicine();
        medicine.setQuantity(medicine.getQuantity() + adjustment);
        medicineRepository.save(medicine);

        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }
}