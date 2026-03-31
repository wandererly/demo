package com.hrm.dto;

import lombok.Data;

@Data
public class PerformanceIndicatorUpdateRequest {
	private String name;
	private Integer weight;
	private String description;
}
