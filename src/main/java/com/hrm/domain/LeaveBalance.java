package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LeaveBalance {
	private Long id;
	private Long empId;
	private String leaveType;
	private BigDecimal totalDays;
	private BigDecimal usedDays;
	private BigDecimal remainingDays;
	private int cycleYear;
	private LocalDateTime updatedAt;
}
