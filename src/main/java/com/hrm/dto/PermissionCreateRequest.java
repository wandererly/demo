package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreateRequest {
	@NotBlank(message = "Permission key is required")
	private String permKey;
	@NotBlank(message = "Permission name is required")
	private String permName;
}
