package com.customermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
public class Customer {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	private UUID id;

	@NotNull(message = "Name is required")
	private String name;

	@NotNull(message = "Email is required")
	@Email(message = "Email should be valid")
	@Column(unique = true)
	private String email;

	private Double annualSpend;

	private LocalDateTime lastPurchaseDate;

	public Customer() {
	}

	public Customer(String name, String email, Double annualSpend, LocalDateTime lastPurchaseDate) {
		this.name = name;
		this.email = email;
		this.annualSpend = annualSpend;
		this.lastPurchaseDate = lastPurchaseDate;
	}
}
