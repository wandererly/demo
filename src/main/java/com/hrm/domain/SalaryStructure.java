package com.hrm.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SalaryStructure {
	private Long id;
	private Long empId;
	private BigDecimal baseSalary;
	private BigDecimal allowance;
	private BigDecimal bonus;
	private BigDecimal socialSecurity;
	private BigDecimal housingFund;
	private BigDecimal taxRate;
}
