package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollCalcRequest {
	@NotNull(message = "Base salary is required")
	private BigDecimal baseSalary;
	@NotNull(message = "Allowance is required")
	private BigDecimal allowance;
	@NotNull(message = "Bonus is required")
	private BigDecimal bonus;
	@NotNull(message = "Overtime hours is required")
	private BigDecimal overtimeHours;
	@NotNull(message = "Overtime rate is required")
	private BigDecimal overtimeRate;
	@NotNull(message = "Leave days is required")
	private BigDecimal leaveDays;
	@NotNull(message = "Leave deduction per day is required")
	private BigDecimal leaveDeductionPerDay;
	@NotNull(message = "Social security is required")
	private BigDecimal socialSecurity;
	@NotNull(message = "Housing fund is required")
	private BigDecimal housingFund;
	private BigDecimal taxRate;
}
