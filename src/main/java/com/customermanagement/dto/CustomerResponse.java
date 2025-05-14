package com.customermanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.customermanagement.enums.MembershipTier;

import lombok.Data;

@Data 
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private Double annualSpend;
    private LocalDateTime lastPurchaseDate;
    private MembershipTier tier; 
}
