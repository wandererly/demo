package com.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceReviewCreateRequest {
	@NotNull(message = "Employee ID is required")
	private Long empId;
	@NotNull(message = "Cycle ID is required")
	private Long cycleId;
	@NotNull(message = "评分不能为空")
	@Min(value = 0, message = "评分不能低于0")
	@Max(value = 100, message = "评分不能超过100")
	private Integer score;
	private String level;
	private Long reviewerId;
	private String goalSummary;
	private String approvalStatus;
	private Long approverId;
	private String comment;
}


