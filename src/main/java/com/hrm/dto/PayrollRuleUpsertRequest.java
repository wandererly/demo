package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollRuleUpsertRequest {
	@NotNull(message = "Overtime multiplier is required")
	private Double overtimeMultiplier;
}
