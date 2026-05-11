package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class WorkShift {
	private Long id;
	private String shiftName;
	private String startTime;
	private String endTime;
	private BigDecimal workHours;
	private String status;
	private LocalDateTime createdAt;
}
