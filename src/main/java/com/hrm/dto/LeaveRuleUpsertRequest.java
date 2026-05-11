package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LeaveRuleUpsertRequest {
	@NotNull(message = "Max days per request is required")
	@PositiveOrZero(message = "单次最大天数不能为负数")
	private Double maxDaysPerRequest;
	@NotNull(message = "Approval level is required")
	private Integer approvalLevel;
}
