package com.hrm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hrm.common.exception.BizException;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PerformanceRule;
import com.hrm.dto.PayrollCalcRequest;
import com.hrm.dto.PerformanceRuleCreateRequest;
import com.hrm.mapper.LeaveRuleMapper;
import com.hrm.mapper.PayrollRecordMapper;
import com.hrm.mapper.PayrollRuleMapper;
import com.hrm.mapper.PayrollTaxBracketMapper;
import com.hrm.mapper.PerformanceRuleMapper;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.service.impl.PayrollServiceImpl;
import com.hrm.service.impl.RuleServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayrollServiceImplTest {

	@Test
	void calculateUsesConfiguredOvertimeMultiplierAndTaxBracket() {
		PayrollRuleMapper payrollRuleMapper = mock(PayrollRuleMapper.class);
		PayrollTaxBracketMapper bracketMapper = mock(PayrollTaxBracketMapper.class);
		PayrollRule rule = new PayrollRule();
		rule.setId(1L);
		rule.setOvertimeMultiplier(1.5);
		when(payrollRuleMapper.findLatest()).thenReturn(rule);
		when(bracketMapper.findByRuleId(1L)).thenReturn(List.of());

		PayrollServiceImpl service = new PayrollServiceImpl(
				mock(SalaryStructureMapper.class), mock(PayrollRecordMapper.class), payrollRuleMapper,
				bracketMapper, mock(AuditLogService.class));
		PayrollCalcRequest request = new PayrollCalcRequest();
		request.setBaseSalary(new BigDecimal("10000"));
		request.setAllowance(new BigDecimal("500"));
		request.setBonus(new BigDecimal("1000"));
		request.setOvertimeHours(new BigDecimal("2"));
		request.setOvertimeRate(new BigDecimal("100"));
		request.setLeaveDays(new BigDecimal("1"));
		request.setLeaveDeductionPerDay(new BigDecimal("200"));
		request.setSocialSecurity(new BigDecimal("500"));
		request.setHousingFund(new BigDecimal("500"));
		request.setTaxRate(new BigDecimal("0.1"));

		var result = service.calculate(request);
		assertEquals(new BigDecimal("300.00"), result.getOvertimePay());
		assertEquals(new BigDecimal("11600.00"), result.getGrossSalary());
		assertEquals(new BigDecimal("9540.00"), result.getNetSalary());
	}

	@Test
	void performanceRulesRejectOverlappingRanges() {
		PerformanceRuleMapper performanceMapper = mock(PerformanceRuleMapper.class);
		PerformanceRule existing = new PerformanceRule();
		existing.setMinScore(60);
		existing.setMaxScore(79);
		when(performanceMapper.findAll()).thenReturn(List.of(existing));
		RuleServiceImpl service = new RuleServiceImpl(
				mock(LeaveRuleMapper.class), performanceMapper, mock(PayrollRuleMapper.class), mock(PayrollTaxBracketMapper.class));
		PerformanceRuleCreateRequest request = new PerformanceRuleCreateRequest();
		request.setMinScore(75);
		request.setMaxScore(90);
		request.setLevel("A");

		assertThrows(BizException.class, () -> service.createPerformanceRule(request));
	}
}
