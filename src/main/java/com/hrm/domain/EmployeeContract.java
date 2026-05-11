package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeeContract {
	private Long id;
	private Long empId;
	private String contractNo;
	private String contractType;
	private LocalDate startDate;
	private LocalDate endDate;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
