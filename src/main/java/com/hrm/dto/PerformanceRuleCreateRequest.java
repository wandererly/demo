package com.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceRuleCreateRequest {
	@NotNull(message = "最低分不能为空")
	@Min(value = 0, message = "最低分不能低于0")
	@Max(value = 100, message = "最低分不能超过100")
	private Integer minScore;
	@NotNull(message = "最高分不能为空")
	@Min(value = 0, message = "最高分不能低于0")
	@Max(value = 100, message = "最高分不能超过100")
	private Integer maxScore;
	@NotBlank(message = "Level is required")
	private String level;
}
