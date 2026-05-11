package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLog {
	private Long id;
	private String module;
	private String action;
	private String targetType;
	private Long targetId;
	private String actor;
	private String detail;
	private String beforeValue;
	private String afterValue;
	private String ipAddress;
	private LocalDateTime createdAt;
}
