package com.hrm.dto;

import lombok.Data;

@Data
public class PerformanceReviewUpdateRequest {
	private Integer score;
	private String level;
	private Long reviewerId;
	private String goalSummary;
	private String approvalStatus;
	private Long approverId;
	private String comment;
}


