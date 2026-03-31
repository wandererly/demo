package com.hrm.auth;

import lombok.Data;

@Data
public class RegisterRequest {
	private String username;
	private String password;
}
