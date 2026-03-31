package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Employee {
	private Long id;
	private String empNo;
	private String name;
	private String gender;
	private String phone;
	private String email;
	private Long deptId;
	private String position;
	private LocalDate hireDate;
	private String status;
	private BigDecimal baseSalary;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
