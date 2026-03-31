package com.hrm.dto;

import lombok.Data;

@Data
public class DepartmentUpdateRequest {
	private String name;
	private Long parentId;
	private Long managerId;
	private String status;
}
