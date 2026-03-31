package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PerformanceCycle {
	private Long id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private String status;
	private LocalDateTime createdAt;
}
