package com.hrm.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PayrollItem {
	private Long id;
	private Long payrollId;
	private String itemType;
	private String itemName;
	private BigDecimal amount;
}
