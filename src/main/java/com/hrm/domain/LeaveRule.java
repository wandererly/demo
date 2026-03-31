package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LeaveRule {
	private Long id;
	private double maxDaysPerRequest;
	private int approvalLevel;
	private LocalDateTime createdAt;
}
