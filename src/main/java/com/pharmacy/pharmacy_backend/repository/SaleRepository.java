package com.pharmacy.pharmacy_backend.repository;

import com.pharmacy.pharmacy_backend.model.Sale;
import com.pharmacy.pharmacy_backend.model.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByStatus(SaleStatus status);

    List<Sale> findByCashierId(Long cashierId);

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    List<Sale> findTodaysSales();

    @Query("SELECT s FROM Sale s WHERE DATE(s.createdAt) = :date")
    List<Sale> findByDate(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE")
    Long countTodaysSales();

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE DATE(s.createdAt) = CURRENT_DATE AND s.status = 'COMPLETED'")
    Double getTodayTotalRevenue();

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE MONTH(s.createdAt) = MONTH(CURRENT_DATE) AND YEAR(s.createdAt) = YEAR(CURRENT_DATE) AND s.status = 'COMPLETED'")
    Double getMonthTotalRevenue();

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE YEAR(s.createdAt) = YEAR(CURRENT_DATE) AND s.status = 'COMPLETED'")
    Double getYearTotalRevenue();

    @Query("SELECT FUNCTION('DATE', s.createdAt), COUNT(s), COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end GROUP BY FUNCTION('DATE', s.createdAt) ORDER BY FUNCTION('DATE', s.createdAt)")
    List<Object[]> getDailySalesReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s), COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.customer.id = :customerId AND s.status = 'COMPLETED'")
    List<Object[]> getCustomerPurchaseStats(@Param("customerId") Long customerId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}