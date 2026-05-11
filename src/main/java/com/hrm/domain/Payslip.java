package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Payslip {
	private Long id;
	private Long empId;
	private String cycleMonth;
	private BigDecimal baseSalary;
	private BigDecimal allowance;
	private BigDecimal bonus;
	private BigDecimal overtimePay;
	private BigDecimal leaveDeduction;
	private BigDecimal socialSecurity;
	private BigDecimal housingFund;
	private BigDecimal tax;
	private BigDecimal grossSalary;
	private BigDecimal netSalary;
	private String performanceLevel;
	private String status;
	private Long approverId;
	private LocalDateTime approvedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
