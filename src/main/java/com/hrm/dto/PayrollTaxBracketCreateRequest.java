package com.hrm.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollTaxBracketCreateRequest {
	@NotNull(message = "Min income is required")
	@DecimalMin(value = "0.00", message = "最低收入不能为负数")
	private BigDecimal minIncome;
	@DecimalMin(value = "0.00", message = "最高收入不能为负数")
	private BigDecimal maxIncome;
	@NotNull(message = "Rate is required")
	@DecimalMin(value = "0.00", message = "税率不能为负数")
	@DecimalMax(value = "1.00", message = "税率不能超过100%")
	private BigDecimal rate;
}
