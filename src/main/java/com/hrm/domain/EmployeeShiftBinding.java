package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeeShiftBinding {
	private Long id;
	private Long empId;
	private Long shiftId;
	private String status;
	private LocalDateTime updatedAt;
}
