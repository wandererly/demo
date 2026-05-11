package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollGenerateRequest {
	@NotNull(message = "员工不能为空")
	private Long empId;
	@NotBlank(message = "薪酬月份不能为空")
	private String cycleMonth;
	@NotNull(message = "加班小时不能为空")
	@DecimalMin(value = "0.00", message = "加班小时不能为负数")
	private BigDecimal overtimeHours;
	@NotNull(message = "加班费率不能为空")
	@DecimalMin(value = "0.00", message = "加班费率不能为负数")
	private BigDecimal overtimeRate;
	@NotNull(message = "请假天数不能为空")
	@DecimalMin(value = "0.00", message = "请假天数不能为负数")
	private BigDecimal leaveDays;
	@NotNull(message = "日扣款不能为空")
	@DecimalMin(value = "0.00", message = "日扣款不能为负数")
	private BigDecimal leaveDeductionPerDay;
}
