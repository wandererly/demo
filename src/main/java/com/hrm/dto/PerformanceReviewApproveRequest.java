package com.hrm.dto;

import lombok.Data;

@Data
public class PerformanceReviewApproveRequest {
	private String approvalStatus;
	private Long approverId;
	private String comment;
}
