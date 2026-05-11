package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {
	@NotBlank(message = "Username is required")
	private String username;
	@NotBlank(message = "Password hash is required")
	private String passwordHash;
	private String role;
	private Long empId;
	private String status;
}
