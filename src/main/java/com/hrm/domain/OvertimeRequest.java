package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OvertimeRequest {
	private Long id;
	private Long empId;
	private LocalDate workDate;
	private BigDecimal hours;
	private String reason;
	private String status;
	private Long approverId;
	private LocalDateTime approvedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
