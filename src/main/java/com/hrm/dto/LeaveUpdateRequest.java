package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LeaveUpdateRequest {
	private String leaveType;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime startTime;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime endTime;
	@PositiveOrZero(message = "请假天数不能为负数")
	private Double days;
	private String reason;
	private String status;
	private Long approverId;
}
