package com.hrm.domain;

import lombok.Data;

@Data
public class PerformanceIndicator {
	private Long id;
	private Long cycleId;
	private String name;
	private int weight;
	private String description;
}
