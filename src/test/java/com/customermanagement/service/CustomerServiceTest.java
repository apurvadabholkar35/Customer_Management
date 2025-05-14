package com.customermanagement.service;

import com.customermanagement.dto.CustomerRequest;
import com.customermanagement.dto.CustomerResponse;
import com.customermanagement.entity.Customer;
import com.customermanagement.enums.MembershipTier;
import com.customermanagement.exception.ResourceNotFoundException;
import com.customermanagement.repository.CustomerRepository;
import com.customermanagement.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
public class CustomerServiceTest {

    @Mock 
    private CustomerRepository customerRepository;

    @InjectMocks 
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;
    private UUID customerId;

    @BeforeEach 
    void setUp() {
        customerId = UUID.randomUUID();
        customer = new Customer("John Doe", "john.doe@example.com", 1500.0, LocalDateTime.now().minusMonths(5));
        customer.setId(customerId);

        customerRequest = new CustomerRequest();
        customerRequest.setName("John Doe");
        customerRequest.setEmail("john.doe@example.com");
        customerRequest.setAnnualSpend(1500.0);
        customerRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(5));
    }

    @Test 
    void createCustomer_shouldReturnCustomerResponse() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer); // Mock repository save method

        CustomerResponse response = customerService.createCustomer(customerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(customerId);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getAnnualSpend()).isEqualTo(1500.0);
        assertThat(response.getTier()).isEqualTo(MembershipTier.GOLD); 
    }

    @Test 
    void getCustomerById_shouldReturnCustomerResponse_whenCustomerExists() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer)); 
        CustomerResponse response = customerService.getCustomerById(customerId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(customerId);
        assertThat(response.getTier()).isEqualTo(MembershipTier.GOLD); 
    }

    @Test 
    void getCustomerById_shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty()); 

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(customerId)); 
    }

    @Test 
    void getAllCustomers_shouldReturnListOfCustomerResponses() {
        Customer customer2 = new Customer("Jane Doe", "jane.doe@example.com", 500.0, LocalDateTime.now().minusMonths(2));
        customer2.setId(UUID.randomUUID());
        List<Customer> customers = Arrays.asList(customer, customer2);

        when(customerRepository.findAll()).thenReturn(customers); 

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertThat(responses).isNotNull().hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(customer.getId());
        assertThat(responses.get(0).getTier()).isEqualTo(MembershipTier.GOLD);
        assertThat(responses.get(1).getId()).isEqualTo(customer2.getId());
        assertThat(responses.get(1).getTier()).isEqualTo(MembershipTier.SILVER); // Verify tier calculation for the second customer
    }

    @Test 
    void updateCustomer_shouldReturnUpdatedCustomerResponse_whenCustomerExists() {
        CustomerRequest updatedRequest = new CustomerRequest();
        updatedRequest.setName("John Updated");
        updatedRequest.setEmail("john.updated@example.com");
        updatedRequest.setAnnualSpend(12000.0);
        updatedRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(3));

        Customer updatedCustomer = new Customer("John Updated", "john.updated@example.com", 12000.0, LocalDateTime.now().minusMonths(3));
        updatedCustomer.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer)); // Mock finding the existing customer
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer); // Mock saving the updated customer

        CustomerResponse response = customerService.updateCustomer(customerId, updatedRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(customerId);
        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getEmail()).isEqualTo("john.updated@example.com");
        assertThat(response.getAnnualSpend()).isEqualTo(12000.0);
        assertThat(response.getTier()).isEqualTo(MembershipTier.PLATINUM); 
    }

    @Test 
    void updateCustomer_shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
        CustomerRequest updatedRequest = new CustomerRequest();
        updatedRequest.setName("John Updated");
        updatedRequest.setEmail("john.updated@example.com");
        updatedRequest.setAnnualSpend(12000.0);
        updatedRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(3));

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty()); // Mock repository findById to return empty

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(customerId, updatedRequest)); // Verify exception is thrown
    }

    @Test 
    void deleteCustomer_shouldDeleteCustomer_whenCustomerExists() {
        when(customerRepository.existsById(customerId)).thenReturn(true); 
        doNothing().when(customerRepository).deleteById(customerId); 

        customerService.deleteCustomer(customerId);

        verify(customerRepository, times(1)).deleteById(customerId); 
    }

    @Test 
    void deleteCustomer_shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
        when(customerRepository.existsById(customerId)).thenReturn(false); 

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(customerId)); 
        verify(customerRepository, never()).deleteById(any(UUID.class)); 
    }


    @Test
    void calculateMembershipTier_shouldReturnSilver_whenAnnualSpendLessThan1000() {
        MembershipTier tier = customerService.calculateMembershipTier(500.0, LocalDateTime.now());
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }

    @Test
    void calculateMembershipTier_shouldReturnSilver_whenAnnualSpendIsNull() {
        MembershipTier tier = customerService.calculateMembershipTier(null, LocalDateTime.now());
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }

    @Test
    void calculateMembershipTier_shouldReturnGold_whenSpendBetween1000And10000AndRecentPurchase() {
        MembershipTier tier = customerService.calculateMembershipTier(5000.0, LocalDateTime.now().minusMonths(10));
        assertThat(tier).isEqualTo(MembershipTier.GOLD);
    }

    @Test
    void calculateMembershipTier_shouldReturnSilver_whenSpendBetween1000And10000AndOldPurchase() {
        MembershipTier tier = customerService.calculateMembershipTier(5000.0, LocalDateTime.now().minusMonths(13));
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }

    @Test
    void calculateMembershipTier_shouldReturnPlatinum_whenSpendGreaterThanOrEqualTo10000AndRecentPurchase() {
        MembershipTier tier = customerService.calculateMembershipTier(12000.0, LocalDateTime.now().minusMonths(5));
        assertThat(tier).isEqualTo(MembershipTier.PLATINUM);
    }

    @Test
    void calculateMembershipTier_shouldReturnGold_whenSpendGreaterThanOrEqualTo10000AndPurchaseWithin12MonthsButNot6() {
         MembershipTier tier = customerService.calculateMembershipTier(12000.0, LocalDateTime.now().minusMonths(8));
         assertThat(tier).isEqualTo(MembershipTier.GOLD);
    }

     @Test
    void calculateMembershipTier_shouldReturnSilver_whenSpendGreaterThanOrEqualTo10000AndOldPurchase() {
        MembershipTier tier = customerService.calculateMembershipTier(12000.0, LocalDateTime.now().minusMonths(13));
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }	

    @Test
    void calculateMembershipTier_shouldReturnSilver_whenSpendBetween1000And10000AndLastPurchaseDateIsNull() {
        MembershipTier tier = customerService.calculateMembershipTier(5000.0, null);
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }

     @Test
    void calculateMembershipTier_shouldReturnSilver_whenSpendGreaterThanOrEqualTo10000AndLastPurchaseDateIsNull() {
        MembershipTier tier = customerService.calculateMembershipTier(12000.0, null);
        assertThat(tier).isEqualTo(MembershipTier.SILVER);
    }
}
