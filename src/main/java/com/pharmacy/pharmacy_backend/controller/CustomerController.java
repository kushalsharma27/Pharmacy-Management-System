package com.pharmacy.pharmacy_backend.controller;

import com.pharmacy.pharmacy_backend.dto.CustomerRequest;
import com.pharmacy.pharmacy_backend.dto.CustomerResponse;
import com.pharmacy.pharmacy_backend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("isAuthenticated()")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(@RequestParam String name) {
        return ResponseEntity.ok(customerService.searchCustomersByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST', 'ROLE_CASHIER')")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/loyalty")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<CustomerResponse> addLoyaltyPoints(
            @PathVariable Long id,
            @RequestParam Integer points) {
        return ResponseEntity.ok(customerService.addLoyaltyPoints(id, points));
    }

    @GetMapping("/top-loyalty")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PHARMACIST')")
    public ResponseEntity<List<CustomerResponse>> getTopCustomersByLoyalty() {
        return ResponseEntity.ok(customerService.getTopCustomersByLoyalty());
    }
}