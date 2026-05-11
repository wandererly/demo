package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceMonthlySummary {
	private Long id;
	private Long empId;
	private String cycleMonth;
	private int presentDays;
	private int lateCount;
	private int earlyLeaveCount;
	private int absentDays;
	private BigDecimal leaveDays;
	private BigDecimal overtimeHours;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
