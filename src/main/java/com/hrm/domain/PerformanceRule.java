package com.hrm.domain;

import lombok.Data;

@Data
public class PerformanceRule {
	private Long id;
	private int minScore;
	private int maxScore;
	private String level;
}
