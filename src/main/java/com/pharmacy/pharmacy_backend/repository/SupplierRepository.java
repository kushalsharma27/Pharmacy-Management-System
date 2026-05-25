package com.pharmacy.pharmacy_backend.repository;

import com.pharmacy.pharmacy_backend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByIsActiveTrue();
    List<Supplier> findByIsActiveFalse();
    List<Supplier> findByNameContainingIgnoreCase(String name);
    Optional<Supplier> findById(Long id);

    @Modifying
    @Query("DELETE FROM Supplier s WHERE s.id = :id")
    void hardDeleteById(@Param("id") Long id);
}