package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollGenerateRequest {
	@NotNull(message = "Employee ID is required")
	private Long empId;
	@NotBlank(message = "Cycle month is required")
	private String cycleMonth;
	@NotNull(message = "Overtime hours is required")
	private BigDecimal overtimeHours;
	@NotNull(message = "Overtime rate is required")
	private BigDecimal overtimeRate;
	@NotNull(message = "Leave days is required")
	private BigDecimal leaveDays;
	@NotNull(message = "Leave deduction per day is required")
	private BigDecimal leaveDeductionPerDay;
}
