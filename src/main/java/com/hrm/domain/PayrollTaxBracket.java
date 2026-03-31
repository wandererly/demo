package com.hrm.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PayrollTaxBracket {
	private Long id;
	private Long ruleId;
	private BigDecimal minIncome;
	private BigDecimal maxIncome;
	private BigDecimal rate;
}
