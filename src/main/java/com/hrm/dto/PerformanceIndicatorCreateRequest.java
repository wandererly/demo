package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceIndicatorCreateRequest {
	@NotNull(message = "Cycle ID is required")
	private Long cycleId;
	@NotBlank(message = "Name is required")
	private String name;
	@NotNull(message = "Weight is required")
	private Integer weight;
	private String description;
}
