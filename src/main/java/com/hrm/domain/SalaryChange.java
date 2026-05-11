package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SalaryChange {
	private Long id;
	private Long empId;
	private BigDecimal beforeSalary;
	private BigDecimal afterSalary;
	private String effectiveMonth;
	private String reason;
	private String status;
	private Long approverId;
	private LocalDateTime approvedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
