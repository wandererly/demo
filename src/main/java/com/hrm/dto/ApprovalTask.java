package com.hrm.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApprovalTask {
	private String type;
	private Long businessId;
	private String title;
	private Long applicantId;
	private String applicantName;
	private String status;
	private Long approverId;
	private String source;
	private LocalDateTime createdAt;
}
