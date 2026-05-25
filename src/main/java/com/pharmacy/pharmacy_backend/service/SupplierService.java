package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.SupplierRequest;
import com.pharmacy.pharmacy_backend.model.Supplier;
import com.pharmacy.pharmacy_backend.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue();
    }

    public List<Supplier> getInactiveSuppliers() {
        return supplierRepository.findByIsActiveFalse();  // This calls your repository method
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier();
        mapRequestToSupplier(request, supplier);
        supplier.setActive(true);  // New suppliers are active by default
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = getSupplierById(id);
        mapRequestToSupplier(request, supplier);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);  // Hard delete
    }

    public List<Supplier> searchSuppliersByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Supplier activateSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier deactivateSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setActive(false);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void hardDeleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.hardDeleteById(id);  // Hard delete using custom query
    }

    private void mapRequestToSupplier(SupplierRequest request, Supplier supplier) {
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setGstNumber(request.getGstNumber());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setNotes(request.getNotes());
    }
}