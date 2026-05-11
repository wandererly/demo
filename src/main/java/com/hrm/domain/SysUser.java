package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysUser {
	private Long id;
	private String username;
	private String passwordHash;
	private String role;
	private Long empId;
	private String status;
	private LocalDateTime createdAt;
}
