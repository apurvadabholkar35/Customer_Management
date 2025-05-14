package com.customermanagement.controller;

import com.customermanagement.dto.CustomerRequest;
import com.customermanagement.dto.CustomerResponse;
import com.customermanagement.exception.ResourceNotFoundException;
import com.customermanagement.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController 
@RequestMapping("/customers") 
@Tag(name = "Customer Management", description = "API for managing customer data") 
public class CustomerController {

    private final CustomerService customerService;

    @Autowired 
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create a new customer",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Customer created successfully",
                                content = @Content(mediaType = "application/json",
                                                   schema = @Schema(implementation = CustomerResponse.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid request"),
                   @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping 
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        CustomerResponse createdCustomer = customerService.createCustomer(customerRequest);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve a customer by ID",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Customer retrieved successfully",
                                content = @Content(mediaType = "application/json",
                                                   schema = @Schema(implementation = CustomerResponse.class))),
                   @ApiResponse(responseCode = "404", description = "Customer not found"),
                   @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{id}") 
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Retrieve all customers",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully",
                                content = @Content(mediaType = "application/json",
                                                   schema = @Schema(implementation = CustomerResponse.class))),
                   @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping 
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Update a customer by ID",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                                content = @Content(mediaType = "application/json",
                                                   schema = @Schema(implementation = CustomerResponse.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid request"),
                   @ApiResponse(responseCode = "404", description = "Customer not found"),
                   @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping("/{id}") 
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable UUID id, @Valid @RequestBody CustomerRequest customerRequest) {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id, customerRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Delete a customer by ID",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Customer not found"),
                   @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "/customers/{id}" 
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @Schema(name = "ErrorResponse", description = "Details about an error response")
    public static class ErrorResponse {
        public LocalDateTime timestamp;
        public int status;
        public String error;
        public String path;

        public ErrorResponse(LocalDateTime timestamp, int status, String error, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.path = path;
        }
    }
}
