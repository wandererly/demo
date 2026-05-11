package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceCorrectionRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotNull(message = "请选择补卡日期")
	private LocalDate workDate;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String reason;
}
