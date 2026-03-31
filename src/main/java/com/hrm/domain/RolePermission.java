package com.hrm.domain;

import lombok.Data;

@Data
public class RolePermission {
	private Long id;
	private Long roleId;
	private Long permId;
}
