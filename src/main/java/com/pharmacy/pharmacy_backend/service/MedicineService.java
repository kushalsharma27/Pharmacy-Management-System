package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.model.Medicine;
import com.pharmacy.pharmacy_backend.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public Medicine addMedicine(Medicine medicine) {
        // Set manufacturing date if not provided
        if (medicine.getManufacturingDate() == null) {
            medicine.setManufacturingDate(LocalDate.now());
        }
        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isLowStock)
                .toList();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isExpired)
                .toList();
    }

    public List<Medicine> getExpiringSoonMedicines(int days) {
        return medicineRepository.findAll().stream()
                .filter(medicine -> medicine.willExpireSoon(days))
                .toList();
    }

    public Medicine updateStock(Long medicineId, Integer quantityChange, String operation) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if ("ADD".equals(operation)) {
            medicine.setQuantity(medicine.getQuantity() + quantityChange);
        } else if ("DEDUCT".equals(operation)) {
            if (medicine.getQuantity() < quantityChange) {
                throw new RuntimeException("Insufficient stock");
            }
            medicine.setQuantity(medicine.getQuantity() - quantityChange);
        }

        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }

    // FIXED: Proper partial update method
    public Medicine updateMedicine(Long id, Medicine updatedMedicine) {
        Medicine existing = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        // ✅ ONLY update non-null fields (PARTIAL UPDATE)
        if (updatedMedicine.getName() != null) {
            existing.setName(updatedMedicine.getName());
        }
        if (updatedMedicine.getGenericName() != null) {
            existing.setGenericName(updatedMedicine.getGenericName());
        }
        if (updatedMedicine.getBatchNumber() != null) {
            existing.setBatchNumber(updatedMedicine.getBatchNumber());
        }
        if (updatedMedicine.getManufacturer() != null) {
            existing.setManufacturer(updatedMedicine.getManufacturer());
        }
        if (updatedMedicine.getPrice() != null) {
            existing.setPrice(updatedMedicine.getPrice());
        }
        if (updatedMedicine.getQuantity() != null) {
            existing.setQuantity(updatedMedicine.getQuantity());
        }
        if (updatedMedicine.getReorderLevel() != null) {
            existing.setReorderLevel(updatedMedicine.getReorderLevel());
        }
        if (updatedMedicine.getExpiryDate() != null) {
            existing.setExpiryDate(updatedMedicine.getExpiryDate());
        }
        if (updatedMedicine.getCategory() != null) {
            existing.setCategory(updatedMedicine.getCategory());
        }
        if (updatedMedicine.getManufacturingDate() != null) {
            existing.setManufacturingDate(updatedMedicine.getManufacturingDate());
        }

        return medicineRepository.save(existing);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
    }

    public List<Medicine> searchMedicinesByName(String name) {
        return medicineRepository.findAll().stream()
                .filter(medicine -> medicine.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
}