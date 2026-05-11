package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceCorrection {
	private Long id;
	private Long empId;
	private LocalDate workDate;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String reason;
	private String status;
	private Long approverId;
	private LocalDateTime approvedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
