package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class JobLevel {
	private Long id;
	private String levelCode;
	private String levelName;
	private Integer rank;
	private BigDecimal salaryMin;
	private BigDecimal salaryMax;
	private String description;
	private String status;
	private LocalDateTime createdAt;
}
