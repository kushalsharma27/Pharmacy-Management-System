package com.pharmacy.pharmacy_backend.service;

import com.pharmacy.pharmacy_backend.dto.CustomerRequest;
import com.pharmacy.pharmacy_backend.dto.CustomerResponse;
import com.pharmacy.pharmacy_backend.model.Customer;
import com.pharmacy.pharmacy_backend.repository.CustomerRepository;
import com.pharmacy.pharmacy_backend.repository.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SaleRepository saleRepository;

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        return convertToResponse(customer);
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with email: " + email));
        return convertToResponse(customer);
    }

    public List<CustomerResponse> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Check if email already exists
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setLoyaltyPoints(0);

        Customer savedCustomer = customerRepository.save(customer);
        return convertToResponse(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customer.setName(request.getName());

        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already registered: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }

        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer updatedCustomer = customerRepository.save(customer);
        return convertToResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public CustomerResponse addLoyaltyPoints(Long id, Integer points) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        Customer updatedCustomer = customerRepository.save(customer);
        return convertToResponse(updatedCustomer);
    }

    public List<CustomerResponse> getTopCustomersByLoyalty() {
        return customerRepository.findTopCustomersByLoyaltyPoints().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse convertToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getLoyaltyPoints(),
                customer.getCreatedAt()
        );

        // Get customer purchase statistics
        List<Object[]> stats = saleRepository.getCustomerPurchaseStats(customer.getId());
        if (stats != null && !stats.isEmpty()) {
            Object[] stat = stats.get(0);
            response.setTotalPurchases((Long) stat[0]);
            response.setTotalSpent((Double) stat[1]);
        }

        return response;
    }
}