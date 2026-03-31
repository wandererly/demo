package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalaryStructureUpsertRequest {
	@NotNull(message = "Employee ID is required")
	private Long empId;
	@NotNull(message = "Base salary is required")
	private BigDecimal baseSalary;
	@NotNull(message = "Allowance is required")
	private BigDecimal allowance;
	@NotNull(message = "Bonus is required")
	private BigDecimal bonus;
	@NotNull(message = "Social security is required")
	private BigDecimal socialSecurity;
	@NotNull(message = "Housing fund is required")
	private BigDecimal housingFund;
	@NotNull(message = "Tax rate is required")
	private BigDecimal taxRate;
}
