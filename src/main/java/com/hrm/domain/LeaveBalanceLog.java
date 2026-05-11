package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LeaveBalanceLog {
	private Long id;
	private Long empId;
	private String leaveType;
	private BigDecimal changeDays;
	private String reason;
	private String refType;
	private Long refId;
	private LocalDateTime createdAt;
}
