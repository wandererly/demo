package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateRequest {
	@NotBlank(message = "Role name is required")
	private String roleName;
	@NotBlank(message = "Role key is required")
	private String roleKey;
}
