package com.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PayslipComponent {
	private Long id;
	private Long payslipId;
	private String componentType;
	private BigDecimal amount;
	private String sourceType;
	private Long sourceId;
	private String remark;
	private LocalDateTime createdAt;
}
