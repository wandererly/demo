package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LifecycleEvent {
	private Long id;
	private Long empId;
	private String eventType;
	private String title;
	private Long fromDeptId;
	private Long toDeptId;
	private String fromPosition;
	private String toPosition;
	private LocalDate effectiveDate;
	private String status;
	private Long approverId;
	private LocalDateTime approvedAt;
	private String detail;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
