package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalDecisionRequest {
	@NotBlank(message = "Approval type is required")
	private String type;
	@NotNull(message = "Business ID is required")
	private Long businessId;
	private Long approverId;
	@NotBlank(message = "Status is required")
	private String status;
	private String comment;
}
