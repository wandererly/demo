package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class PayrollRuleUpsertRequest {
	@NotNull(message = "Overtime multiplier is required")
	@PositiveOrZero(message = "加班倍率不能为负数")
	private Double overtimeMultiplier;
}
