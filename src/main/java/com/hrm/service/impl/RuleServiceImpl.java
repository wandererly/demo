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
import java.math.BigDecimal;
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
		if (request.getMaxDaysPerRequest() == null || request.getMaxDaysPerRequest() <= 0) {
			throw new BizException(ErrorCode.BAD_REQUEST, "单次最大请假天数必须大于0");
		}
		if (request.getApprovalLevel() == null || request.getApprovalLevel() < 1 || request.getApprovalLevel() > 2) {
			throw new BizException(ErrorCode.BAD_REQUEST, "审批级别只能是1或2级");
		}
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
		if (request.getMinScore() > request.getMaxScore()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "最低分不能高于最高分");
		}
		for (PerformanceRule existing : performanceRuleMapper.findAll()) {
			if (request.getMinScore() <= existing.getMaxScore() && request.getMaxScore() >= existing.getMinScore()) {
				throw new BizException(ErrorCode.BAD_REQUEST, "绩效分数区间与现有规则重叠");
			}
		}
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
		if (request.getOvertimeMultiplier() == null || request.getOvertimeMultiplier() <= 0 || request.getOvertimeMultiplier() > 5) {
			throw new BizException(ErrorCode.BAD_REQUEST, "加班倍率必须在0到5之间");
		}
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
		if (ruleId == null || payrollRuleMapper.findById(ruleId) == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "薪酬规则不存在");
		}
		if (request.getMinIncome() == null || request.getMinIncome().compareTo(BigDecimal.ZERO) < 0
				|| (request.getMaxIncome() != null && request.getMaxIncome().compareTo(request.getMinIncome()) <= 0)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "税率区间无效，最高收入必须大于最低收入");
		}
		if (request.getRate() == null || request.getRate().compareTo(BigDecimal.ZERO) < 0
				|| request.getRate().compareTo(BigDecimal.ONE) > 0) {
			throw new BizException(ErrorCode.BAD_REQUEST, "税率必须在0到100%之间");
		}
		for (PayrollTaxBracket existing : payrollTaxBracketMapper.findByRuleId(ruleId)) {
			BigDecimal existingMax = existing.getMaxIncome();
			boolean overlaps = (existingMax == null || request.getMinIncome().compareTo(existingMax) < 0)
					&& (request.getMaxIncome() == null || existing.getMinIncome().compareTo(request.getMaxIncome()) < 0);
			if (overlaps) {
				throw new BizException(ErrorCode.BAD_REQUEST, "税率收入区间与现有规则重叠");
			}
		}
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
