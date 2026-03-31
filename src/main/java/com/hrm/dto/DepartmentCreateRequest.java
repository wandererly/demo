package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreateRequest {
	@NotBlank(message = "Department name is required")
	private String name;
	private Long parentId;
	private Long managerId;
	private String status;
}
