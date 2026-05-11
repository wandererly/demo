package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PerformanceGoal {
	private Long id;
	private Long empId;
	private Long cycleId;
	private String goalTitle;
	private String targetValue;
	private Integer selfScore;
	private Integer managerScore;
	private Integer finalScore;
	private String finalLevel;
	private String status;
	private Long reviewerId;
	private LocalDateTime calibratedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
