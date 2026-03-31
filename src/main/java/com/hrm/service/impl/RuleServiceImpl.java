package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.LeaveRule;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PayrollTaxBracket;
import com.hrm.domain.PerformanceRule;
import com.hrm.dto.LeaveRuleUpsertRequest;
import com.hrm.dto.PayrollRuleUpsertRequest;
import com.hrm.dto.PayrollTaxBracketCreateRequest;
import com.hrm.dto.PerformanceRuleCreateRequest;
import com.hrm.mapper.LeaveRuleMapper;
import com.hrm.mapper.PayrollRuleMapper;
import com.hrm.mapper.PayrollTaxBracketMapper;
import com.hrm.mapper.PerformanceRuleMapper;
import com.hrm.service.RuleService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RuleServiceImpl implements RuleService {

	private final LeaveRuleMapper leaveRuleMapper;
	private final PerformanceRuleMapper performanceRuleMapper;
	private final PayrollRuleMapper payrollRuleMapper;
	private final PayrollTaxBracketMapper payrollTaxBracketMapper;

	public RuleServiceImpl(LeaveRuleMapper leaveRuleMapper,
						  PerformanceRuleMapper performanceRuleMapper,
						  PayrollRuleMapper payrollRuleMapper,
						  PayrollTaxBracketMapper payrollTaxBracketMapper) {
		this.leaveRuleMapper = leaveRuleMapper;
		this.performanceRuleMapper = performanceRuleMapper;
		this.payrollRuleMapper = payrollRuleMapper;
		this.payrollTaxBracketMapper = payrollTaxBracketMapper;
	}

	@Override
	public LeaveRule getLeaveRule() {
		return leaveRuleMapper.findLatest();
	}

	@Override
	public LeaveRule upsertLeaveRule(LeaveRuleUpsertRequest request) {
		LeaveRule existing = leaveRuleMapper.findLatest();
		LeaveRule rule = new LeaveRule();
		rule.setMaxDaysPerRequest(request.getMaxDaysPerRequest());
		rule.setApprovalLevel(request.getApprovalLevel());
		if (existing == null) {
			leaveRuleMapper.insert(rule);
		} else {
			rule.setId(existing.getId());
			leaveRuleMapper.update(rule);
		}
		return rule;
	}

	@Override
	public List<PerformanceRule> listPerformanceRules() {
		return performanceRuleMapper.findAll();
	}

	@Override
	public PerformanceRule createPerformanceRule(PerformanceRuleCreateRequest request) {
		PerformanceRule rule = new PerformanceRule();
		rule.setMinScore(request.getMinScore());
		rule.setMaxScore(request.getMaxScore());
		rule.setLevel(request.getLevel());
		performanceRuleMapper.insert(rule);
		return rule;
	}

	@Override
	public void deletePerformanceRule(Long id) {
		int rows = performanceRuleMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance rule not found");
		}
	}

	@Override
	public PayrollRule getPayrollRule() {
		return payrollRuleMapper.findLatest();
	}

	@Override
	public PayrollRule upsertPayrollRule(PayrollRuleUpsertRequest request) {
		PayrollRule existing = payrollRuleMapper.findLatest();
		PayrollRule rule = new PayrollRule();
		rule.setOvertimeMultiplier(request.getOvertimeMultiplier());
		if (existing == null) {
			payrollRuleMapper.insert(rule);
		} else {
			rule.setId(existing.getId());
			payrollRuleMapper.update(rule);
		}
		return rule;
	}

	@Override
	public List<PayrollTaxBracket> listPayrollTaxBrackets(Long ruleId) {
		return payrollTaxBracketMapper.findByRuleId(ruleId);
	}

	@Override
	public PayrollTaxBracket createPayrollTaxBracket(Long ruleId, PayrollTaxBracketCreateRequest request) {
		PayrollTaxBracket bracket = new PayrollTaxBracket();
		bracket.setRuleId(ruleId);
		bracket.setMinIncome(request.getMinIncome());
		bracket.setMaxIncome(request.getMaxIncome());
		bracket.setRate(request.getRate());
		payrollTaxBracketMapper.insert(bracket);
		return bracket;
	}

	@Override
	public void deletePayrollTaxBracket(Long id) {
		int rows = payrollTaxBracketMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Tax bracket not found");
		}
	}
}
