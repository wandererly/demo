package com.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Holiday {
	private Long id;
	private LocalDate holidayDate;
	private String holidayName;
	private String holidayType;
	private LocalDateTime createdAt;
}
