package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LeaveRequest {
	private Long id;
	private Long empId;
	private String leaveType;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private double days;
	private String reason;
	private String status;
	private Long approverId;
	private LocalDateTime createdAt;
}
