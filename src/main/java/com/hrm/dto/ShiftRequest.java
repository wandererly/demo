package com.hrm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ShiftRequest {
	@NotBlank(message = "请输入班次名称")
	private String shiftName;
	@NotBlank(message = "请输入上班时间")
	private String startTime;
	@NotBlank(message = "请输入下班时间")
	private String endTime;
	@NotNull(message = "请输入工时")
	@DecimalMin(value = "1.0", message = "工时必须大于0")
	private BigDecimal workHours;
	private String status;
}
