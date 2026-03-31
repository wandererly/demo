package com.hrm.service;

import com.hrm.domain.LeaveRule;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PayrollTaxBracket;
import com.hrm.domain.PerformanceRule;
import com.hrm.dto.LeaveRuleUpsertRequest;
import com.hrm.dto.PayrollRuleUpsertRequest;
import com.hrm.dto.PayrollTaxBracketCreateRequest;
import com.hrm.dto.PerformanceRuleCreateRequest;
import java.util.List;

public interface RuleService {
	LeaveRule getLeaveRule();
	LeaveRule upsertLeaveRule(LeaveRuleUpsertRequest request);

	List<PerformanceRule> listPerformanceRules();
	PerformanceRule createPerformanceRule(PerformanceRuleCreateRequest request);
	void deletePerformanceRule(Long id);

	PayrollRule getPayrollRule();
	PayrollRule upsertPayrollRule(PayrollRuleUpsertRequest request);
	List<PayrollTaxBracket> listPayrollTaxBrackets(Long ruleId);
	PayrollTaxBracket createPayrollTaxBracket(Long ruleId, PayrollTaxBracketCreateRequest request);
	void deletePayrollTaxBracket(Long id);
}
