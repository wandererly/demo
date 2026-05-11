package com.hrm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OvertimeRequestDto {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotNull(message = "请选择加班日期")
	private LocalDate workDate;
	@NotNull(message = "请输入加班小时")
	@DecimalMin(value = "0.0", message = "加班小时不能为负数")
	private BigDecimal hours;
	private String reason;
}
