package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LeaveCreateRequest {
	@NotNull(message = "员工ID不能为空")
	private Long empId;
	@NotBlank(message = "请假类型不能为空")
	private String leaveType;
	@NotNull(message = "开始时间不能为空")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime startTime;
	@NotNull(message = "结束时间不能为空")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime endTime;
	@NotNull(message = "请假天数不能为空")
	@PositiveOrZero(message = "请假天数不能为负数")
	private Double days;
	private String reason;
}
