package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceGoalRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotNull(message = "请选择绩效周期")
	private Long cycleId;
	@NotBlank(message = "请输入绩效目标")
	private String goalTitle;
	private String targetValue;
	private Long reviewerId;
}
