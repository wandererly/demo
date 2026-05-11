package com.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceIndicatorCreateRequest {
	@NotNull(message = "Cycle ID is required")
	private Long cycleId;
	@NotBlank(message = "Name is required")
	private String name;
	@NotNull(message = "权重不能为空")
	@Min(value = 0, message = "权重不能低于0")
	@Max(value = 100, message = "权重不能超过100")
	private Integer weight;
	private String description;
}
