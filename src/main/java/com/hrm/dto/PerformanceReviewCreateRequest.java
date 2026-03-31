package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceReviewCreateRequest {
	@NotNull(message = "Employee ID is required")
	private Long empId;
	@NotNull(message = "Cycle ID is required")
	private Long cycleId;
	@NotNull(message = "Score is required")
	private Integer score;
	private String level;
	private Long reviewerId;
	private String goalSummary;
	private String approvalStatus;
	private Long approverId;
	private String comment;
}


