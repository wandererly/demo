package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceCreateRequest {
	@NotNull(message = "员工ID不能为空")
	private Long empId;
	@NotNull(message = "工作日期不能为空")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate workDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime checkIn;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime checkOut;
	private String status;
}
