package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PerformanceReview {
	private Long id;
	private Long empId;
	private Long cycleId;
	private int score;
	private String level;
	private Long reviewerId;
	private String goalSummary;
	private String approvalStatus;
	private Long approverId;
	private LocalDateTime approvedAt;
	private String comment;
	private LocalDateTime createdAt;
}


