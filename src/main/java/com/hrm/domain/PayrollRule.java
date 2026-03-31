package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PayrollRule {
	private Long id;
	private double overtimeMultiplier;
	private LocalDateTime createdAt;
}
