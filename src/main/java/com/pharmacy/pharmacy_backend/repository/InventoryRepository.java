package com.pharmacy.pharmacy_backend.repository;

import com.pharmacy.pharmacy_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByMedicineId(Long medicineId);

    List<Inventory> findByBatchNumber(String batchNumber);

    List<Inventory> findBySupplierId(Long supplierId);

    List<Inventory> findByPurchaseOrderId(Long purchaseOrderId);

    List<Inventory> findByExpiryDateBefore(LocalDate date);

    List<Inventory> findByExpiryDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    List<Inventory> findLowStockItems(@Param("threshold") int threshold);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.medicine.id = :medicineId")
    Integer getTotalQuantityByMedicine(@Param("medicineId") Long medicineId);

    List<Inventory> findByStorageLocation(String location);

    long countByExpiryDateBefore(LocalDate date);
}