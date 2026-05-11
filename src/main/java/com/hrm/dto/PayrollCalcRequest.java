package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollCalcRequest {
	@NotNull(message = "基本工资不能为空")
	@DecimalMin(value = "3500.00", message = "基本工资不能低于3500")
	private BigDecimal baseSalary;
	@NotNull(message = "津贴不能为空")
	@DecimalMin(value = "0.00", message = "津贴不能为负数")
	private BigDecimal allowance;
	@NotNull(message = "奖金不能为空")
	@DecimalMin(value = "0.00", message = "奖金不能为负数")
	private BigDecimal bonus;
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
	@NotNull(message = "社保不能为空")
	@DecimalMin(value = "0.00", message = "社保不能为负数")
	private BigDecimal socialSecurity;
	@NotNull(message = "公积金不能为空")
	@DecimalMin(value = "0.00", message = "公积金不能为负数")
	private BigDecimal housingFund;
	@DecimalMin(value = "0.00", message = "税率不能为负数")
	@DecimalMax(value = "1.00", message = "税率不能超过100%")
	private BigDecimal taxRate;
}
