package com.customermanagement.controller;

import com.customermanagement.dto.CustomerRequest;
import com.customermanagement.dto.CustomerResponse;
import com.customermanagement.enums.MembershipTier;
import com.customermanagement.exception.ResourceNotFoundException;
import com.customermanagement.main.CustomerManagementApplication;
import com.customermanagement.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Import AutoConfigureMockMvc


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK,classes = {CustomerController.class, CustomerManagementApplication.class})
@ContextConfiguration(classes = CustomerManagementApplication.class)
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @MockBean 
    private CustomerService customerService;

    private ObjectMapper objectMapper; 
    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;
    private UUID customerId;

    @BeforeEach 
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register module for Java 8 Date/Time support

        customerId = UUID.randomUUID();

        customerRequest = new CustomerRequest();
        customerRequest.setName("John Doe");
        customerRequest.setEmail("john.doe@example.com");
        customerRequest.setAnnualSpend(1500.0);
        customerRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(5));

        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setName("John Doe");
        customerResponse.setEmail("john.doe@example.com");
        customerResponse.setAnnualSpend(1500.0);
        customerResponse.setLastPurchaseDate(LocalDateTime.now().minusMonths(5));
        customerResponse.setTier(MembershipTier.GOLD);
    }

    @Test // Tests the createCustomer endpoint
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(customerResponse); // Mock service method

        mockMvc.perform(post("/customers") // Perform POST request
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest))) // Set request body
                .andExpect(status().isCreated()) // Expect 201 Created status
                .andExpect(jsonPath("$.id").value(customerId.toString())) // Expect specific JSON values in response
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.tier").value("GOLD"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequest.class)); // Verify service method was called once
    }

    @Test // Tests the getCustomerById endpoint when customer exists
    void getCustomerById_shouldReturnCustomer_whenCustomerExists() throws Exception {
        when(customerService.getCustomerById(customerId)).thenReturn(customerResponse); // Mock service method

        mockMvc.perform(get("/customers/{id}", customerId) // Perform GET request with path variable
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect 200 OK status
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.tier").value("GOLD"));

        verify(customerService, times(1)).getCustomerById(customerId); // Verify service method was called once
    }

    @Test // Tests the getCustomerById endpoint when customer does not exist
    void getCustomerById_shouldReturnNotFound_whenCustomerDoesNotExist() throws Exception {
        when(customerService.getCustomerById(customerId)).thenThrow(new ResourceNotFoundException("Customer not found")); // Mock service to throw exception

        mockMvc.perform(get("/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect 404 Not Found status

        verify(customerService, times(1)).getCustomerById(customerId); // Verify service method was called once
    }

    @Test // Tests the getAllCustomers endpoint
    void getAllCustomers_shouldReturnListOfCustomers() throws Exception {
        CustomerResponse customer2Response = new CustomerResponse();
        customer2Response.setId(UUID.randomUUID());
        customer2Response.setName("Jane Doe");
        customer2Response.setEmail("jane.doe@example.com");
        customer2Response.setAnnualSpend(500.0);
        customer2Response.setLastPurchaseDate(LocalDateTime.now().minusMonths(2));
        customer2Response.setTier(MembershipTier.SILVER);

        List<CustomerResponse> customerList = Arrays.asList(customerResponse, customer2Response);

        when(customerService.getAllCustomers()).thenReturn(customerList); // Mock service method

        mockMvc.perform(get("/customers") // Perform GET request
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect 200 OK status
                .andExpect(jsonPath("$.length()").value(2)) // Expect a list of size 2
                .andExpect(jsonPath("$[0].id").value(customerId.toString()))
                .andExpect(jsonPath("$[0].tier").value("GOLD"))
                .andExpect(jsonPath("$[1].id").value(customer2Response.getId().toString()))
                .andExpect(jsonPath("$[1].tier").value("SILVER"));

        verify(customerService, times(1)).getAllCustomers(); // Verify service method was called once
    }

    @Test // Tests the updateCustomer endpoint when customer exists
    void updateCustomer_shouldReturnUpdatedCustomer_whenCustomerExists() throws Exception {
        CustomerRequest updatedRequest = new CustomerRequest();
        updatedRequest.setName("John Updated");
        updatedRequest.setEmail("john.updated@example.com");
        updatedRequest.setAnnualSpend(12000.0);
        updatedRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(3));

        CustomerResponse updatedResponse = new CustomerResponse();
        updatedResponse.setId(customerId);
        updatedResponse.setName("John Updated");
        updatedResponse.setEmail("john.updated@example.com");
        updatedResponse.setAnnualSpend(12000.0);
        updatedResponse.setLastPurchaseDate(LocalDateTime.now().minusMonths(3));
        updatedResponse.setTier(MembershipTier.PLATINUM);

        when(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class))).thenReturn(updatedResponse); // Mock service method

        mockMvc.perform(put("/customers/{id}", customerId) // Perform PUT request
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRequest))) // Set request body
                .andExpect(status().isOk()) // Expect 200 OK status
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.tier").value("PLATINUM"));

        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerRequest.class)); // Verify service method was called once
    }

    @Test // Tests the updateCustomer endpoint when customer does not exist
    void updateCustomer_shouldReturnNotFound_whenCustomerDoesNotExist() throws Exception {
        CustomerRequest updatedRequest = new CustomerRequest();
        updatedRequest.setName("John Updated");
        updatedRequest.setEmail("john.updated@example.com");
        updatedRequest.setAnnualSpend(12000.0);
        updatedRequest.setLastPurchaseDate(LocalDateTime.now().minusMonths(3));

        when(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class))).thenThrow(new ResourceNotFoundException("Customer not found")); // Mock service to throw exception

        mockMvc.perform(put("/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isNotFound()); // Expect 404 Not Found status

        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerRequest.class)); // Verify service method was called once
    }

    @Test // Tests the deleteCustomer endpoint when customer exists
    void deleteCustomer_shouldReturnNoContent_whenCustomerExists() throws Exception {
        doNothing().when(customerService).deleteCustomer(customerId); // Mock service method to do nothing

        mockMvc.perform(delete("/customers/{id}", customerId) // Perform DELETE request
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Expect 204 No Content status

        verify(customerService, times(1)).deleteCustomer(customerId); // Verify service method was called once
    }

    @Test // Tests the deleteCustomer endpoint when customer does not exist
    void deleteCustomer_shouldReturnNotFound_whenCustomerDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Customer not found")).when(customerService).deleteCustomer(customerId); // Mock service to throw exception

        mockMvc.perform(delete("/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect 404 Not Found status

        verify(customerService, times(1)).deleteCustomer(customerId); // Verify service method was called once
    }

    @Test // Tests validation for createCustomer endpoint (missing name)
    void createCustomer_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setEmail("test@example.com"); // Missing name

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); 

        verify(customerService, never()).createCustomer(any(CustomerRequest.class)); 
    }

     @Test 
    void createCustomer_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("invalid-email"); 

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); 

        verify(customerService, never()).createCustomer(any(CustomerRequest.class)); 
    }
}
