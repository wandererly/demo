package com.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateRequest {
	@NotBlank(message = "工号不能为空")
	private String empNo;
	@NotBlank(message = "姓名不能为空")
	private String name;
	private String gender;
	private String phone;
	private String email;
	@NotNull(message = "部门不能为空")
	private Long deptId;
	private String position;
	@NotNull(message = "入职日期不能为空")
	@JsonFormat(pattern = "yyyy-M-d")
	private LocalDate hireDate;
	private String status;
	@NotNull(message = "基础薪资不能为空")
	private BigDecimal baseSalary;
}





