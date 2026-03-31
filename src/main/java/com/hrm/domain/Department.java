package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Department {
	private Long id;
	private String name;
	private Long parentId;
	private Long managerId;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
