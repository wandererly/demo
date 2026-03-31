package com.hrm.auth;

import java.util.List;
import lombok.Data;

@Data
public class LoginResponse {
	private String token;
	private String username;
	private String role;
	private List<String> permissions;
}
