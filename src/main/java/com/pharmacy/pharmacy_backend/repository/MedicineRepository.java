package com.pharmacy.pharmacy_backend.repository;

import com.pharmacy.pharmacy_backend.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    List<Medicine> findByQuantityLessThanEqual(Integer reorderLevel);

    List<Medicine> findByExpiryDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT m FROM Medicine m WHERE m.quantity <= m.reorderLevel")
    List<Medicine> findLowStockMedicines();

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate BETWEEN CURRENT_DATE AND :endDate")
    List<Medicine> findExpiringMedicines(@Param("endDate") LocalDate endDate);
}