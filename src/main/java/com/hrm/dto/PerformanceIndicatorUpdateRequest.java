package com.hrm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PerformanceIndicatorUpdateRequest {
	private String name;
	@Min(value = 0, message = "权重不能低于0")
	@Max(value = 100, message = "权重不能超过100")
	private Integer weight;
	private String description;
}
