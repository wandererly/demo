package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.Employee;
import com.hrm.dto.EmployeeCreateRequest;
import com.hrm.dto.EmployeeUpdateRequest;
import com.hrm.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Validated
@PreAuthorize("hasAuthority('hr:manage')")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public ApiResponse<List<Employee>> list() {
		return ApiResponse.ok(employeeService.list());
	}

	@GetMapping("/{id}")
	public ApiResponse<Employee> get(@PathVariable Long id) {
		return ApiResponse.ok(employeeService.get(id));
	}

	@PostMapping
	public ApiResponse<Employee> create(@Valid @RequestBody EmployeeCreateRequest request) {
		return ApiResponse.ok(employeeService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<Employee> update(@PathVariable Long id, @RequestBody EmployeeUpdateRequest request) {
		return ApiResponse.ok(employeeService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		employeeService.delete(id);
		return ApiResponse.ok(null);
	}
}



