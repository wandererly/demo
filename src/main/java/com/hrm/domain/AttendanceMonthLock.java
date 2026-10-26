package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceMonthLock {
	private Long id;
	private String cycleMonth;
	private String status;
	private Long lockedBy;
	private LocalDateTime lockedAt;
	private String remark;
}
