package com.pharmacy.pharmacy_backend.repository;

import com.pharmacy.pharmacy_backend.model.PurchaseOrder;
import com.pharmacy.pharmacy_backend.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> findByStatusIn(List<PurchaseOrderStatus> statuses);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDelivery < CURRENT_DATE " +
            "AND po.status NOT IN ('RECEIVED', 'CANCELLED')")
    List<PurchaseOrder> findOverduePurchaseOrders();

    @Query("SELECT COALESCE(SUM(po.totalAmount), 0.0) FROM PurchaseOrder po")
    Double sumTotalAmount();

    @Query("SELECT COALESCE(SUM(po.totalAmount), 0.0) FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    Double sumTotalAmountBySupplier(@Param("supplierId") Long supplierId);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status")
    Long countByStatus(@Param("status") PurchaseOrderStatus status);

    /**
     * FUTURE FEATURE: Filter purchase orders by supplier AND status
     * Use case: Procurement dashboard - "Show all PENDING orders from Supplier X"
     */
    @SuppressWarnings("unused")
    List<PurchaseOrder> findBySupplierIdAndStatus(Long supplierId, PurchaseOrderStatus status);

    /**
     * FUTURE FEATURE: Daily order count for reporting
     * Use case: Admin dashboard - "How many orders placed today?"
     */
    @SuppressWarnings("unused")
    long countByOrderDateBetween(LocalDate startDate, LocalDate endDate);
}