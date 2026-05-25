package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.*;
import com.pharmacy.pharmacy_backend.model.*;
import com.pharmacy.pharmacy_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InventoryService inventoryService;

    public List<SaleResponse> getAllSales() {
        return saleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + id));
        return convertToResponse(sale);
    }

    public SaleResponse getSaleByInvoiceNumber(String invoiceNumber) {
        Sale sale = saleRepository.findAll().stream()
                .filter(s -> s.getInvoiceNumber().equals(invoiceNumber))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with invoice: " + invoiceNumber));
        return convertToResponse(sale);
    }

    public List<SaleResponse> getSalesByStatus(String status) {
        try {
            SaleStatus saleStatus = SaleStatus.valueOf(status.toUpperCase());
            return saleRepository.findByStatus(saleStatus).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    public List<SaleResponse> getSalesByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return saleRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return saleRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getTodaysSales() {
        return saleRepository.findTodaysSales().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getSalesByCashier(Long cashierId) {
        return saleRepository.findByCashierId(cashierId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ============ ENHANCED CREATE SALE WITH MULTI-PAYMENT ============

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        // Get current logged-in user as cashier
        User cashier = getCurrentUser();

        // VALIDATION STEP 1: Validate stock availability
        validateStockAvailability(request);

        // VALIDATION STEP 2: Check for expired medicines
        validateMedicineExpiry(request);

        // Create new sale
        Sale sale = new Sale();
        sale.setCashier(cashier);
        sale.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0.0);
        sale.setTax(0.0);
        sale.setNotes(request.getNotes());
        sale.setStatus(SaleStatus.PENDING);

        // Set customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + request.getCustomerId()));
            sale.setCustomer(customer);
        }

        // Process items and update inventory
        double subtotal = processSaleItems(sale, request.getItems());

        sale.setSubtotal(subtotal);
        sale.setGrandTotal(subtotal - sale.getDiscount() + sale.getTax());

        // VALIDATION STEP 3: Validate and process payments
        List<PaymentDetails> payments = getEffectivePayments(request);
        if (payments.isEmpty()) {
            throw new RuntimeException("Payment details are required");
        }

        // Check if total payment is sufficient
        double totalPaid = calculateTotalPaid(payments);
        if (totalPaid < sale.getGrandTotal() - 0.01) { // Small epsilon for floating point
            throw new RuntimeException(String.format(
                    "Insufficient payment. Total: %.2f, Paid: %.2f, Short: %.2f",
                    sale.getGrandTotal(), totalPaid, (sale.getGrandTotal() - totalPaid)
            ));
        }

        // Save sale first
        Sale savedSale = saleRepository.save(sale);

        // VALIDATION STEP 4: Process each payment method with specific validation
        processPayments(savedSale, payments);

        // Update sale with payment info
        double cashAmount = payments.stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.CASH)
                .mapToDouble(PaymentDetails::getAmount)
                .sum();

        savedSale.setAmountPaid(totalPaid);
        savedSale.setChangeAmount(Math.max(0, cashAmount - (sale.getGrandTotal() - (totalPaid - cashAmount))));

        // Set primary payment method for backward compatibility
        if (!payments.isEmpty()) {
            savedSale.setPaymentMethod(payments.get(0).getPaymentMethod().toString());
        }

        savedSale.setStatus(SaleStatus.COMPLETED);

        // VALIDATION STEP 5: Check for low stock alerts
        checkLowStockAlerts();

        Sale finalSale = saleRepository.save(savedSale);
        return convertToResponse(finalSale);
    }

    // ============ VALIDATION METHODS ============

    private void validateStockAvailability(SaleRequest request) {
        for (SaleItemRequest item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + item.getMedicineId()));

            // Check if medicine exists and has stock
            if (medicine.getQuantity() == null || medicine.getQuantity() <= 0) {
                throw new RuntimeException("Medicine '" + medicine.getName() + "' is out of stock");
            }

            // Check sufficient quantity
            if (medicine.getQuantity() < item.getQuantity()) {
                throw new RuntimeException(String.format(
                        "Insufficient stock for '%s'. Available: %d, Requested: %d",
                        medicine.getName(), medicine.getQuantity(), item.getQuantity()
                ));
            }
        }
    }

    private void validateMedicineExpiry(SaleRequest request) {
        for (SaleItemRequest item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + item.getMedicineId()));

            // Check if medicine is expired
            if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Medicine '" + medicine.getName() + "' is expired. Expiry date: " +
                        medicine.getExpiryDate());
            }

            // Warning for near-expiry (log only, don't block sale)
            if (medicine.getExpiryDate() != null &&
                    medicine.getExpiryDate().isBefore(LocalDate.now().plusDays(30))) {
                System.out.println("WARNING: Medicine '" + medicine.getName() +
                        "' expires soon on: " + medicine.getExpiryDate());
            }
        }
    }

    private List<PaymentDetails> getEffectivePayments(SaleRequest request) {
        // If using new multi-payment format
        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            return request.getPayments();
        }

        // Backward compatibility: create single payment from legacy fields
        if (request.getPaymentMethod() != null && request.getAmountPaid() != null) {
            List<PaymentDetails> legacyPayments = new ArrayList<>();
            PaymentDetails payment = new PaymentDetails();

            try {
                payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                payment.setPaymentMethod(PaymentMethod.CASH);
            }

            payment.setAmount(request.getAmountPaid());
            legacyPayments.add(payment);
            return legacyPayments;
        }

        return new ArrayList<>();
    }

    private double calculateTotalPaid(List<PaymentDetails> payments) {
        return payments.stream()
                .mapToDouble(PaymentDetails::getAmount)
                .sum();
    }

    private void processPayments(Sale sale, List<PaymentDetails> payments) {
        for (PaymentDetails paymentDto : payments) {
            // Validate payment method specific requirements
            validatePaymentDetails(paymentDto, sale.getCustomer());

            Payment payment = new Payment();
            payment.setSale(sale);
            payment.setPaymentMethod(paymentDto.getPaymentMethod());
            payment.setAmount(paymentDto.getAmount());

            // Set method-specific fields
            switch (paymentDto.getPaymentMethod()) {
                case CARD:
                    if (paymentDto.getCardNumber() != null && paymentDto.getCardNumber().length() >= 4) {
                        payment.setCardLastFour("****" + paymentDto.getCardNumber()
                                .substring(paymentDto.getCardNumber().length() - 4));
                    }
                    payment.setCardHolderName(paymentDto.getCardHolderName());
                    break;

                case UPI:
                    payment.setUpiId(paymentDto.getUpiId());
                    break;

                case CHEQUE:
                    payment.setChequeNumber(paymentDto.getChequeNumber());
                    payment.setBankName(paymentDto.getBankName());
                    break;

                case INSURANCE:
                    payment.setInsuranceProvider(paymentDto.getInsuranceProvider());
                    payment.setPolicyNumber(paymentDto.getPolicyNumber());
                    break;

                case LOYALTY_POINTS:
                    payment.setLoyaltyPointsUsed(paymentDto.getLoyaltyPointsUsed().intValue());
                    break;

                default:
                    // CASH and others don't need extra fields
                    break;
            }

            payment.setReferenceNumber(paymentDto.getReferenceNumber());
            paymentRepository.save(payment);
        }
    }

    private void validatePaymentDetails(PaymentDetails payment, Customer customer) {
        if (payment.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be positive");
        }

        switch (payment.getPaymentMethod()) {
            case CARD:
                if (payment.getCardNumber() == null || payment.getCardNumber().length() < 4) {
                    throw new RuntimeException("Valid card details are required");
                }
                break;

            case UPI:
                if (payment.getUpiId() == null || !payment.getUpiId().contains("@")) {
                    throw new RuntimeException("Valid UPI ID is required (e.g., name@bank)");
                }
                break;

            case CHEQUE:
                if (payment.getChequeNumber() == null || payment.getBankName() == null) {
                    throw new RuntimeException("Cheque number and bank name are required");
                }
                break;

            case INSURANCE:
                if (payment.getInsuranceProvider() == null || payment.getPolicyNumber() == null) {
                    throw new RuntimeException("Insurance provider and policy number are required");
                }
                break;

            case LOYALTY_POINTS:
                if (payment.getLoyaltyPointsUsed() == null || payment.getLoyaltyPointsUsed() <= 0) {
                    throw new RuntimeException("Valid loyalty points are required");
                }

                // Verify customer has enough points
                if (customer == null) {
                    throw new RuntimeException("Customer required for loyalty points payment");
                }

                int pointsNeeded = payment.getLoyaltyPointsUsed().intValue();
                if (pointsNeeded > customer.getLoyaltyPoints()) {
                    throw new RuntimeException(String.format(
                            "Insufficient loyalty points. Available: %d, Required: %d",
                            customer.getLoyaltyPoints(), pointsNeeded
                    ));
                }

                // Deduct points
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() - pointsNeeded);
                customerRepository.save(customer);
                break;

            default:
                // CASH and others pass through
                break;
        }
    }

    private double processSaleItems(Sale sale, List<SaleItemRequest> items) {
        double subtotal = 0.0;

        for (SaleItemRequest itemRequest : items) {
            Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                    .orElseThrow(() -> new EntityNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

            SaleItem item = new SaleItem();
            item.setMedicine(medicine);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(medicine.getPrice());
            item.setBatchNumber(itemRequest.getBatchNumber());
            item.setDiscount(0.0);
            item.calculateTotal();

            sale.addItem(item);
            subtotal += item.getTotalPrice();

            // Update medicine stock
            medicine.setQuantity(medicine.getQuantity() - itemRequest.getQuantity());
            medicineRepository.save(medicine);
        }

        return subtotal;
    }

    private void checkLowStockAlerts() {
        List<Medicine> lowStock = medicineRepository.findLowStockMedicines();

        if (!lowStock.isEmpty()) {
            System.out.println("⚠️ LOW STOCK ALERT: " + lowStock.size() + " medicines need reordering:");
            lowStock.forEach(m -> System.out.println("   - " + m.getName() +
                    " (Current: " + m.getQuantity() + ", Reorder at: " + m.getReorderLevel() + ")"));
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ============ EXISTING METHODS (UPDATED) ============

    @Transactional
    public SaleResponse updateSaleStatus(Long id, String status) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + id));

        try {
            SaleStatus newStatus = SaleStatus.valueOf(status.toUpperCase());

            // If cancelling, restore inventory
            if (newStatus == SaleStatus.CANCELLED && sale.getStatus() != SaleStatus.CANCELLED) {
                restoreInventory(sale);
            }

            sale.setStatus(newStatus);
            Sale updatedSale = saleRepository.save(sale);
            return convertToResponse(updatedSale);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    private void restoreInventory(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Medicine medicine = item.getMedicine();
            medicine.setQuantity(medicine.getQuantity() + item.getQuantity());
            medicineRepository.save(medicine);
        }

        // Also restore loyalty points if used
        if (sale.getCustomer() != null) {
            List<Payment> loyaltyPayments = paymentRepository.findBySaleId(sale.getId()).stream()
                    .filter(p -> p.getPaymentMethod() == PaymentMethod.LOYALTY_POINTS)
                    .collect(Collectors.toList());

            for (Payment payment : loyaltyPayments) {
                if (payment.getLoyaltyPointsUsed() != null) {
                    Customer customer = sale.getCustomer();
                    customer.setLoyaltyPoints(customer.getLoyaltyPoints() + payment.getLoyaltyPointsUsed());
                    customerRepository.save(customer);
                }
            }
        }
    }

    @Transactional
    public SaleResponse processPayment(PaymentRequest request) {
        Sale sale = saleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + request.getSaleId()));

        // Create payment record
        PaymentDetails paymentDto = new PaymentDetails();
        paymentDto.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        paymentDto.setAmount(request.getAmount());

        List<PaymentDetails> payments = new ArrayList<>();
        payments.add(paymentDto);

        processPayments(sale, payments);

        sale.setAmountPaid(request.getAmount());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setChangeAmount(Math.max(0, request.getAmount() - sale.getGrandTotal()));

        if (sale.getAmountPaid() >= sale.getGrandTotal()) {
            sale.setStatus(SaleStatus.COMPLETED);
        }

        Sale updatedSale = saleRepository.save(sale);
        return convertToResponse(updatedSale);
    }

    // ============ ENHANCED RESPONSE CONVERSION ============

    private SaleResponse convertToResponse(Sale sale) {
        SaleResponse response = new SaleResponse();
        response.setId(sale.getId());
        response.setInvoiceNumber(sale.getInvoiceNumber());
        response.setSubtotal(sale.getSubtotal());
        response.setDiscount(sale.getDiscount());
        response.setTax(sale.getTax());
        response.setGrandTotal(sale.getGrandTotal());
        response.setAmountPaid(sale.getAmountPaid());
        response.setChangeAmount(sale.getChangeAmount());
        response.setPaymentMethod(sale.getPaymentMethod());
        response.setStatus(sale.getStatus().toString());
        response.setCreatedAt(sale.getCreatedAt());
        response.setNotes(sale.getNotes());

        if (sale.getCustomer() != null) {
            response.setCustomerId(sale.getCustomer().getId());
            response.setCustomerName(sale.getCustomer().getName());
        }

        if (sale.getCashier() != null) {
            response.setCashierName(sale.getCashier().getUsername());
        }

        List<SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(this::convertItemToResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private SaleItemResponse convertItemToResponse(SaleItem item) {
        SaleItemResponse response = new SaleItemResponse();
        response.setId(item.getId());
        response.setMedicineId(item.getMedicine().getId());
        response.setMedicineName(item.getMedicine().getName());
        response.setBatchNumber(item.getBatchNumber());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        response.setDiscount(item.getDiscount());
        return response;
    }

    // ============ EXISTING SUMMARY METHODS ============

    public SaleSummaryResponse getTodaySummary() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Sale> todaysSales = saleRepository.findByCreatedAtBetween(start, end);

        Long totalSales = (long) todaysSales.size();
        Double totalRevenue = todaysSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .mapToDouble(Sale::getGrandTotal)
                .sum();
        Long totalItems = todaysSales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToLong(SaleItem::getQuantity)
                .sum();
        Long completedSales = todaysSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .count();
        Long cancelledSales = todaysSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.CANCELLED)
                .count();
        Double avgOrderValue = completedSales > 0 ? totalRevenue / completedSales : 0.0;

        return new SaleSummaryResponse(
                totalSales, totalRevenue, avgOrderValue,
                totalItems, completedSales, cancelledSales,
                LocalDate.now().toString()
        );
    }

    public SaleSummaryResponse getDateRangeSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end);

        Long totalSales = (long) sales.size();
        Double totalRevenue = sales.stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .mapToDouble(Sale::getGrandTotal)
                .sum();
        Long totalItems = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToLong(SaleItem::getQuantity)
                .sum();
        Long completedSales = sales.stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .count();
        Long cancelledSales = sales.stream()
                .filter(s -> s.getStatus() == SaleStatus.CANCELLED)
                .count();
        Double avgOrderValue = completedSales > 0 ? totalRevenue / completedSales : 0.0;

        return new SaleSummaryResponse(
                totalSales, totalRevenue, avgOrderValue,
                totalItems, completedSales, cancelledSales,
                startDate + " to " + endDate
        );
    }

    public List<Object[]> getDailySalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return saleRepository.getDailySalesReport(start, end);
    }
}