package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveRuleUpsertRequest {
	@NotNull(message = "Max days per request is required")
	private Double maxDaysPerRequest;
	@NotNull(message = "Approval level is required")
	private Integer approvalLevel;
}
