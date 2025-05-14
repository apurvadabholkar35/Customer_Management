package com.customermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.customermanagement.entity.Customer;

import java.util.UUID;

@Repository 
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
