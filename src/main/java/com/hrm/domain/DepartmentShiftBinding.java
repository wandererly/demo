package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentShiftBinding {
	private Long id;
	private Long deptId;
	private Long shiftId;
	private String status;
	private LocalDateTime updatedAt;
}
