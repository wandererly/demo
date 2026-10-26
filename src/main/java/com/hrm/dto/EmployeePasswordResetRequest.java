package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeePasswordResetRequest {
	@NotBlank(message = "请输入员工编号")
	private String empNo;

	@NotBlank(message = "请输入新密码")
	private String password;
}
