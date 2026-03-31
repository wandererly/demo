package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
	private String name;
	private String gender;
	private String phone;
	private String email;
	private Long deptId;
	private String position;
	@JsonFormat(pattern = "yyyy-M-d")
	private LocalDate hireDate;
	private String status;
	private BigDecimal baseSalary;
}




