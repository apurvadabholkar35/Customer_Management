package com.studentmanagement.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StudentDto {

	public StudentDto(long l, String string, String string2, int i, LocalDate minusMonths, Object object) {
	}

	private Long id;

	private String name;

	private String email;

	private LocalDate admissiondate;
	private String grade;
	private Integer markobtained;

}