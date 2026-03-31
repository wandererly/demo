package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollTaxBracketCreateRequest {
	@NotNull(message = "Min income is required")
	private BigDecimal minIncome;
	private BigDecimal maxIncome;
	@NotNull(message = "Rate is required")
	private BigDecimal rate;
}
