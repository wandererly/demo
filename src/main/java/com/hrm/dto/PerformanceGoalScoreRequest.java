package com.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PerformanceGoalScoreRequest {
	@Min(value = 0, message = "评分不能低于0")
	@Max(value = 100, message = "评分不能超过100")
	private Integer score;
	private Long reviewerId;
	private String finalLevel;
}
