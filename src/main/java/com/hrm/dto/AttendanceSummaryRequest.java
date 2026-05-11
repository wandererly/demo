package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttendanceSummaryRequest {
	@NotBlank(message = "请选择月份")
	private String cycleMonth;
	private Long empId;
}
