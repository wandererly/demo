package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class LifecycleEventRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotBlank(message = "请选择生命周期类型")
	private String eventType;
	private String title;
	private Long toDeptId;
	private String toPosition;
	private LocalDate effectiveDate;
	private String detail;
}
