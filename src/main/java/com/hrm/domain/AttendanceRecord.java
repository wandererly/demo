package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceRecord {
	private Long id;
	private Long empId;
	private LocalDate workDate;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String status;
	private LocalDateTime createdAt;
}
