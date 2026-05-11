package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HolidayRequest {
	@NotNull(message = "请选择节假日日期")
	private LocalDate holidayDate;
	@NotBlank(message = "请输入节假日名称")
	private String holidayName;
	@NotBlank(message = "请选择节假日类型")
	private String holidayType;
}
