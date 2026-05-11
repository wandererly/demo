package com.hrm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SalaryChangeRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotNull(message = "请输入调整后薪资")
	@DecimalMin(value = "3500.0", message = "调整后基本工资不能低于3500")
	private BigDecimal afterSalary;
	@NotBlank(message = "请选择生效月份")
	private String effectiveMonth;
	private String reason;
}
