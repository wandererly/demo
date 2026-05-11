package com.hrm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LeaveBalanceRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotBlank(message = "请选择假期类型")
	private String leaveType;
	@NotNull(message = "请输入年度额度")
	@DecimalMin(value = "0.0", message = "假期额度不能为负数")
	private BigDecimal totalDays;
	@Min(value = 2000, message = "年度不正确")
	private int cycleYear;
}
