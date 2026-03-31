package com.hrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleAssignRequest {
	@NotNull(message = "User ID is required")
	private Long userId;
	@NotNull(message = "Role ID is required")
	private Long roleId;
}
