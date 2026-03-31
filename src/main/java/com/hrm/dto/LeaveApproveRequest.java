package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveApproveRequest {
	@NotNull(message = "Approver ID is required")
	private Long approverId;
	@NotBlank(message = "Status is required")
	private String status;
}
