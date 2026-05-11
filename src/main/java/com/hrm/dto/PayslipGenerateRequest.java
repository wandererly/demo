package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayslipGenerateRequest {
	@NotBlank(message = "请选择薪酬月份")
	private String cycleMonth;
	private Long empId;
}
