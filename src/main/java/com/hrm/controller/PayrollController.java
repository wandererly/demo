package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.PayrollRecord;
import com.hrm.dto.PayrollCalcRequest;
import com.hrm.dto.PayrollCalcResult;
import com.hrm.dto.PayrollGenerateRequest;
import com.hrm.service.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
@Validated
@PreAuthorize("hasAuthority('payroll:manage')")
public class PayrollController {

	private final PayrollService payrollService;

	public PayrollController(PayrollService payrollService) {
		this.payrollService = payrollService;
	}

	@PostMapping("/calc")
	public ApiResponse<PayrollCalcResult> calculate(@Valid @RequestBody PayrollCalcRequest request) {
		return ApiResponse.ok(payrollService.calculate(request));
	}

	@PostMapping("/generate")
	public ApiResponse<PayrollRecord> generate(@Valid @RequestBody PayrollGenerateRequest request) {
		return ApiResponse.ok(payrollService.generate(request));
	}

	@GetMapping("/employee/{empId}")
	public ApiResponse<List<PayrollRecord>> listByEmp(@PathVariable Long empId) {
		return ApiResponse.ok(payrollService.listByEmp(empId));
	}
}



