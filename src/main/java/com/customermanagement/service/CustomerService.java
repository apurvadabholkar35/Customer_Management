package com.customermanagement.service;

import com.customermanagement.dto.CustomerRequest;
import com.customermanagement.dto.CustomerResponse;
import com.customermanagement.entity.Customer;
import com.customermanagement.enums.MembershipTier;
import com.customermanagement.exception.ResourceNotFoundException;
import com.customermanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	@Autowired
	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public CustomerResponse createCustomer(CustomerRequest customerRequest) {
		Customer customer = new Customer(customerRequest.getName(), customerRequest.getEmail(),
				customerRequest.getAnnualSpend(), customerRequest.getLastPurchaseDate());
		Customer savedCustomer = customerRepository.save(customer);
		return mapToCustomerResponse(savedCustomer);
	}

	public CustomerResponse getCustomerById(UUID id) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
		return mapToCustomerResponse(customer);
	}

	// Retrieves all customers
	public List<CustomerResponse> getAllCustomers() {
		List<Customer> customers = customerRepository.findAll();
		return customers.stream().map(this::mapToCustomerResponse).collect(Collectors.toList());
	}

	// Updates an existing customer
	public CustomerResponse updateCustomer(UUID id, CustomerRequest customerRequest) {
		Customer existingCustomer = customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

		existingCustomer.setName(customerRequest.getName());
		existingCustomer.setEmail(customerRequest.getEmail());
		existingCustomer.setAnnualSpend(customerRequest.getAnnualSpend());
		existingCustomer.setLastPurchaseDate(customerRequest.getLastPurchaseDate());

		Customer updatedCustomer = customerRepository.save(existingCustomer);
		return mapToCustomerResponse(updatedCustomer);
	}

	// Deletes a customer by ID
	public void deleteCustomer(UUID id) {
		if (!customerRepository.existsById(id)) {
			throw new ResourceNotFoundException("Customer not found with id: " + id);
		}
		customerRepository.deleteById(id);
	}

	private CustomerResponse mapToCustomerResponse(Customer customer) {
		CustomerResponse response = new CustomerResponse();
		response.setId(customer.getId());
		response.setName(customer.getName());
		response.setEmail(customer.getEmail());
		response.setAnnualSpend(customer.getAnnualSpend());
		response.setLastPurchaseDate(customer.getLastPurchaseDate());
		response.setTier(calculateMembershipTier(customer.getAnnualSpend(), customer.getLastPurchaseDate()));
		return response;
	}

	public MembershipTier calculateMembershipTier(Double annualSpend, LocalDateTime lastPurchaseDate) {
		if (annualSpend == null || annualSpend < 1000) {
			return MembershipTier.SILVER;
		} else if (annualSpend >= 10000) {
			if (lastPurchaseDate != null && lastPurchaseDate.isAfter(LocalDateTime.now().minusMonths(6))) {
				return MembershipTier.PLATINUM;
			} else {
				if (lastPurchaseDate != null && lastPurchaseDate.isAfter(LocalDateTime.now().minusMonths(12))) {
					return MembershipTier.GOLD;
				} else {
					return MembershipTier.SILVER;
				}
			}
		} else if (annualSpend >= 1000) {
			if (lastPurchaseDate != null && lastPurchaseDate.isAfter(LocalDateTime.now().minusMonths(12))) {
				return MembershipTier.GOLD;
			} else {
				return MembershipTier.SILVER;
			}
		} else {
			return MembershipTier.SILVER;
		}
	}
}
