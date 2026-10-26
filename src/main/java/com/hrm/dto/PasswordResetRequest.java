package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
	@NotBlank(message = "请输入新密码")
	private String password;
}
