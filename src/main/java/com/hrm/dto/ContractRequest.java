package com.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ContractRequest {
	@NotNull(message = "请选择员工")
	private Long empId;
	@NotBlank(message = "请输入合同编号")
	private String contractNo;
	@NotBlank(message = "请输入合同类型")
	private String contractType;
	@NotNull(message = "请选择开始日期")
	private LocalDate startDate;
	private LocalDate endDate;
	private String status;
}
