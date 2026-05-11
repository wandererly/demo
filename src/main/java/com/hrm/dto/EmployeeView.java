package com.hrm.dto;

import com.hrm.domain.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeeView {
	private Long id;
	private String empNo;
	private String name;
	private String gender;
	private String phone;
	private String email;
	private Long deptId;
	private String position;
	private LocalDate hireDate;
	private String status;
	private BigDecimal baseSalary;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String accountUsername;

	public static EmployeeView from(Employee employee, String accountUsername) {
		EmployeeView view = new EmployeeView();
		view.setId(employee.getId());
		view.setEmpNo(employee.getEmpNo());
		view.setName(employee.getName());
		view.setGender(employee.getGender());
		view.setPhone(employee.getPhone());
		view.setEmail(employee.getEmail());
		view.setDeptId(employee.getDeptId());
		view.setPosition(employee.getPosition());
		view.setHireDate(employee.getHireDate());
		view.setStatus(employee.getStatus());
		view.setBaseSalary(employee.getBaseSalary());
		view.setCreatedAt(employee.getCreatedAt());
		view.setUpdatedAt(employee.getUpdatedAt());
		view.setAccountUsername(accountUsername);
		return view;
	}
}
