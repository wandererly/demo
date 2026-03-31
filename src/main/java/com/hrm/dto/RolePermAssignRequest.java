package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RolePermAssignRequest {
	@NotBlank(message = "Role key is required")
	private String roleKey;
	@NotBlank(message = "Permission key is required")
	private String permKey;
}
