package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceRuleCreateRequest {
	@NotNull(message = "Min score is required")
	private Integer minScore;
	@NotNull(message = "Max score is required")
	private Integer maxScore;
	@NotBlank(message = "Level is required")
	private String level;
}
