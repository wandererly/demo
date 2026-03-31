package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.LeaveRule;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PayrollTaxBracket;
import com.hrm.domain.PerformanceRule;
import com.hrm.dto.LeaveRuleUpsertRequest;
import com.hrm.dto.PayrollRuleUpsertRequest;
import com.hrm.dto.PayrollTaxBracketCreateRequest;
import com.hrm.dto.PerformanceRuleCreateRequest;
import com.hrm.service.RuleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
@Validated
@PreAuthorize("hasAuthority('config:manage')")
public class RuleController {

	private final RuleService ruleService;

	public RuleController(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	@GetMapping("/leave")
	public ApiResponse<LeaveRule> getLeaveRule() {
		return ApiResponse.ok(ruleService.getLeaveRule());
	}

	@PostMapping("/leave")
	public ApiResponse<LeaveRule> upsertLeaveRule(@Valid @RequestBody LeaveRuleUpsertRequest request) {
		return ApiResponse.ok(ruleService.upsertLeaveRule(request));
	}

	@GetMapping("/performance")
	public ApiResponse<List<PerformanceRule>> listPerformanceRules() {
		return ApiResponse.ok(ruleService.listPerformanceRules());
	}

	@PostMapping("/performance")
	public ApiResponse<PerformanceRule> createPerformanceRule(@Valid @RequestBody PerformanceRuleCreateRequest request) {
		return ApiResponse.ok(ruleService.createPerformanceRule(request));
	}

	@DeleteMapping("/performance/{id}")
	public ApiResponse<Void> deletePerformanceRule(@PathVariable Long id) {
		ruleService.deletePerformanceRule(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/payroll")
	public ApiResponse<PayrollRule> getPayrollRule() {
		return ApiResponse.ok(ruleService.getPayrollRule());
	}

	@PostMapping("/payroll")
	public ApiResponse<PayrollRule> upsertPayrollRule(@Valid @RequestBody PayrollRuleUpsertRequest request) {
		return ApiResponse.ok(ruleService.upsertPayrollRule(request));
	}

	@GetMapping("/payroll/{ruleId}/brackets")
	public ApiResponse<List<PayrollTaxBracket>> listPayrollBrackets(@PathVariable Long ruleId) {
		return ApiResponse.ok(ruleService.listPayrollTaxBrackets(ruleId));
	}

	@PostMapping("/payroll/{ruleId}/brackets")
	public ApiResponse<PayrollTaxBracket> createPayrollBracket(@PathVariable Long ruleId,
												@Valid @RequestBody PayrollTaxBracketCreateRequest request) {
		return ApiResponse.ok(ruleService.createPayrollTaxBracket(ruleId, request));
	}

	@DeleteMapping("/payroll/brackets/{id}")
	public ApiResponse<Void> deletePayrollBracket(@PathVariable Long id) {
		ruleService.deletePayrollTaxBracket(id);
		return ApiResponse.ok(null);
	}
}



