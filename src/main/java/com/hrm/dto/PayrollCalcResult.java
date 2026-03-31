package com.hrm.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PayrollCalcResult {
	private BigDecimal baseSalary;
	private BigDecimal allowance;
	private BigDecimal bonus;
	private BigDecimal socialSecurity;
	private BigDecimal housingFund;
	private BigDecimal taxableIncome;
	private BigDecimal grossSalary;
	private BigDecimal tax;
	private BigDecimal netSalary;
	private BigDecimal overtimePay;
	private BigDecimal leaveDeduction;
}


