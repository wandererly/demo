package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.SalaryStructure;
import com.hrm.dto.SalaryStructureUpsertRequest;
import com.hrm.service.SalaryStructureService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salary")
@Validated
@PreAuthorize("hasAuthority('payroll:manage')")
public class SalaryStructureController {

	private final SalaryStructureService salaryStructureService;

	public SalaryStructureController(SalaryStructureService salaryStructureService) {
		this.salaryStructureService = salaryStructureService;
	}

	@GetMapping("/structure/{empId}")
	public ApiResponse<SalaryStructure> getByEmp(@PathVariable Long empId) {
		return ApiResponse.ok(salaryStructureService.getByEmpId(empId));
	}

	@PostMapping("/structure")
	public ApiResponse<SalaryStructure> upsert(@Valid @RequestBody SalaryStructureUpsertRequest request) {
		return ApiResponse.ok(salaryStructureService.upsert(request));
	}
}



