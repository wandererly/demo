package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceUpdateRequest {
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate workDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime checkIn;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime checkOut;
	private String status;
}
