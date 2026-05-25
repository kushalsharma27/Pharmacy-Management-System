package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.PurchaseOrderRequest;
import com.pharmacy.pharmacy_backend.dto.PurchaseOrderResponse;
import com.pharmacy.pharmacy_backend.model.*;
import com.pharmacy.pharmacy_backend.repository.PurchaseOrderRepository;
import com.pharmacy.pharmacy_backend.repository.SupplierRepository;
import com.pharmacy.pharmacy_backend.repository.MedicineRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with id: " + id));
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @Transactional
    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        purchaseOrder.calculateTotal();
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public void deletePurchaseOrder(Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("Purchase order not found with id: " + id);
        }
        purchaseOrderRepository.deleteById(id);
    }

    @Transactional
    public PurchaseOrder createPurchaseOrderFromRequest(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setExpectedDelivery(request.getExpectedDelivery());
        purchaseOrder.setPaymentMethod(request.getPaymentMethod());
        purchaseOrder.setNotes(request.getNotes());
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setOrderDate(LocalDate.now());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (var itemRequest : request.getItems()) {
                Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                        .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setMedicine(medicine);
                item.setQuantity(itemRequest.getQuantity());
                item.setPurchasePrice(itemRequest.getPurchasePrice());
                item.setBatchNumber(itemRequest.getBatchNumber());
                item.setExpiryDate(itemRequest.getExpiryDate());

                purchaseOrder.addItem(item);
            }
        }

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status) {
        PurchaseOrder order = getPurchaseOrderById(id);
        order.setStatus(status);
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrderRequest request) {
        PurchaseOrder existingOrder = getPurchaseOrderById(id);

        if (existingOrder.getStatus() == PurchaseOrderStatus.RECEIVED ||
                existingOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a " + existingOrder.getStatus() + " order");
        }

        existingOrder.setExpectedDelivery(request.getExpectedDelivery());
        existingOrder.setPaymentMethod(request.getPaymentMethod());
        existingOrder.setNotes(request.getNotes());

        List<PurchaseOrderItem> itemsToRemove = new ArrayList<>(existingOrder.getItems());
        for (PurchaseOrderItem item : itemsToRemove) {
            existingOrder.removeItem(item);
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (var itemRequest : request.getItems()) {
                Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                        .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setMedicine(medicine);
                item.setQuantity(itemRequest.getQuantity());
                item.setPurchasePrice(itemRequest.getPurchasePrice());
                item.setBatchNumber(itemRequest.getBatchNumber());
                item.setExpiryDate(itemRequest.getExpiryDate());

                existingOrder.addItem(item);
            }
        }

        return purchaseOrderRepository.save(existingOrder);
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrderItem(Long orderId, Long itemId, int receivedQuantity,
                                                  String batchNumber, LocalDate expiryDate) {
        PurchaseOrder order = getPurchaseOrderById(orderId);

        PurchaseOrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Purchase order item not found with id: " + itemId));

        item.setReceivedQuantity(receivedQuantity);
        if (batchNumber != null) item.setBatchNumber(batchNumber);
        if (expiryDate != null) item.setExpiryDate(expiryDate);

        if (item.isFullyReceived()) {
            Medicine medicine = item.getMedicine();

            // Get current quantity (handle null)
            int currentQuantity = medicine.getQuantity() != null ? medicine.getQuantity() : 0;
            medicine.setQuantity(currentQuantity + receivedQuantity);

            // Update last restocked date
            medicine.setLastRestockedDate(LocalDate.now());

            medicineRepository.save(medicine);
        }

        // Check if all items are received
        boolean allReceived = order.getItems().stream().allMatch(PurchaseOrderItem::isFullyReceived);
        if (allReceived) {
            order.setStatus(PurchaseOrderStatus.RECEIVED);
        }

        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrderResponse getPurchaseOrderResponseById(Long id) {
        PurchaseOrder order = getPurchaseOrderById(id);
        return purchaseOrderMapper.toResponse(order);
    }

    public List<PurchaseOrderResponse> getAllPurchaseOrderResponses() {
        List<PurchaseOrder> orders = getAllPurchaseOrders();
        return orders.stream()
                .map(purchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrder savedOrder = createPurchaseOrderFromRequest(request);
        return purchaseOrderMapper.toResponse(savedOrder);
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrderWithResponse(Long id, PurchaseOrderRequest request) {
        PurchaseOrder updatedOrder = updatePurchaseOrder(id, request);
        return purchaseOrderMapper.toResponse(updatedOrder);
    }

    @Transactional
    public PurchaseOrderResponse updateStatusWithResponse(Long id, PurchaseOrderStatus status) {
        PurchaseOrder updatedOrder = updatePurchaseOrderStatus(id, status);
        return purchaseOrderMapper.toResponse(updatedOrder);
    }

    @Transactional
    public PurchaseOrderResponse receiveItemWithResponse(Long orderId, Long itemId, int receivedQuantity,
                                                         String batchNumber, LocalDate expiryDate) {
        PurchaseOrder updatedOrder = receivePurchaseOrderItem(orderId, itemId, receivedQuantity, batchNumber, expiryDate);
        return purchaseOrderMapper.toResponse(updatedOrder);
    }

    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplierId(supplierId);
        return orders.stream()
                .map(purchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrderStatus status) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByStatus(status);
        return orders.stream()
                .map(purchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<PurchaseOrderResponse> getOrdersNeedingAction() {
        // Get DRAFT and PENDING orders
        List<PurchaseOrderStatus> statuses = Arrays.asList(
                PurchaseOrderStatus.DRAFT,
                PurchaseOrderStatus.PENDING
        );
        List<PurchaseOrder> orders = purchaseOrderRepository.findByStatusIn(statuses);
        return orders.stream()
                .map(purchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PurchaseOrderResponse> getOverduePurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findOverduePurchaseOrders();
        return orders.stream()
                .map(purchaseOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public double getTotalPurchaseAmount() {
        Double total = purchaseOrderRepository.sumTotalAmount();
        return total != null ? total : 0.0;
    }

    public double getTotalPurchaseAmountBySupplier(Long supplierId) {
        Double total = purchaseOrderRepository.sumTotalAmountBySupplier(supplierId);
        return total != null ? total : 0.0;
    }

    /**
     * FUTURE FEATURE: Monthly purchase reports
     * Currently UNUSED - kept for future dashboard implementation
     */
    @SuppressWarnings("unused")
    public double getMonthlyPurchaseAmount(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<PurchaseOrder> orders = purchaseOrderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .mapToDouble(PurchaseOrder::getTotalAmount)
                .sum();
    }

    public long getPurchaseOrderCountByStatus(PurchaseOrderStatus status) {
        Long count = purchaseOrderRepository.countByStatus(status);
        return count != null ? count : 0L;
    }

    /**
     * FUTURE FEATURE: Admin dashboard statistics
     * Currently UNUSED - kept for future analytics implementation
     */
    @SuppressWarnings("unused")
    public List<Object[]> getPurchaseStatistics() {
        List<Object[]> stats = new ArrayList<>();
        for (PurchaseOrderStatus status : PurchaseOrderStatus.values()) {
            long count = getPurchaseOrderCountByStatus(status);
            stats.add(new Object[]{status.name(), count});
        }
        return stats;
    }
}