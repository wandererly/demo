package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class JobPosition {
	private Long id;
	private Long deptId;
	private Long levelId;
	private String positionCode;
	private String positionName;
	private String pathGroup;
	private String description;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
