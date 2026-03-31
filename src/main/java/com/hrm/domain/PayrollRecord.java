package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PayrollRecord {
	private Long id;
	private Long empId;
	private String cycleMonth;
	private BigDecimal grossSalary;
	private BigDecimal netSalary;
	private BigDecimal tax;
	private BigDecimal overtimePay;
	private BigDecimal leaveDeduction;
	private String status;
	private LocalDateTime createdAt;
}
