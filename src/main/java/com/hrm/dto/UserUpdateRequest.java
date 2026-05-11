package com.hrm.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
	private String username;
	private String passwordHash;
	private String role;
	private Long empId;
	private String status;
}
