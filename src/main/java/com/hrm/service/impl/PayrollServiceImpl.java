package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.PayrollRecord;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PayrollTaxBracket;
import com.hrm.domain.SalaryStructure;
import com.hrm.dto.PayrollCalcRequest;
import com.hrm.dto.PayrollCalcResult;
import com.hrm.dto.PayrollGenerateRequest;
import com.hrm.mapper.PayrollRecordMapper;
import com.hrm.mapper.PayrollRuleMapper;
import com.hrm.mapper.PayrollTaxBracketMapper;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.service.PayrollService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PayrollServiceImpl implements PayrollService {

	private final SalaryStructureMapper salaryStructureMapper;
	private final PayrollRecordMapper payrollRecordMapper;
	private final PayrollRuleMapper payrollRuleMapper;
	private final PayrollTaxBracketMapper payrollTaxBracketMapper;

	public PayrollServiceImpl(SalaryStructureMapper salaryStructureMapper,
							  PayrollRecordMapper payrollRecordMapper,
							  PayrollRuleMapper payrollRuleMapper,
							  PayrollTaxBracketMapper payrollTaxBracketMapper) {
		this.salaryStructureMapper = salaryStructureMapper;
		this.payrollRecordMapper = payrollRecordMapper;
		this.payrollRuleMapper = payrollRuleMapper;
		this.payrollTaxBracketMapper = payrollTaxBracketMapper;
	}

	@Override
	public PayrollCalcResult calculate(PayrollCalcRequest request) {
		PayrollRule rule = payrollRuleMapper.findLatest();
		double multiplier = rule == null ? 1.0 : rule.getOvertimeMultiplier();

		BigDecimal baseSalary = nz(request.getBaseSalary());
		BigDecimal allowance = nz(request.getAllowance());
		BigDecimal bonus = nz(request.getBonus());
		BigDecimal overtimeHours = nz(request.getOvertimeHours());
		BigDecimal overtimeRate = nz(request.getOvertimeRate());
		BigDecimal leaveDays = nz(request.getLeaveDays());
		BigDecimal leaveDeductionPerDay = nz(request.getLeaveDeductionPerDay());
		BigDecimal socialSecurity = nz(request.getSocialSecurity());
		BigDecimal housingFund = nz(request.getHousingFund());

		BigDecimal overtimePay = overtimeHours
				.multiply(overtimeRate)
				.multiply(BigDecimal.valueOf(multiplier))
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal leaveDeduction = leaveDays
				.multiply(leaveDeductionPerDay)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal gross = baseSalary
				.add(allowance)
				.add(bonus)
				.add(overtimePay)
				.subtract(leaveDeduction)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal taxable = gross
				.subtract(socialSecurity)
				.subtract(housingFund)
				.max(BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal tax = resolveTax(taxable, request.getTaxRate(), rule);

		BigDecimal net = gross
				.subtract(socialSecurity)
				.subtract(housingFund)
				.subtract(tax)
				.setScale(2, RoundingMode.HALF_UP);

		PayrollCalcResult result = new PayrollCalcResult();
		result.setBaseSalary(baseSalary);
		result.setAllowance(allowance);
		result.setBonus(bonus);
		result.setSocialSecurity(socialSecurity);
		result.setHousingFund(housingFund);
		result.setTaxableIncome(taxable);
		result.setGrossSalary(gross);
		result.setTax(tax);
		result.setNetSalary(net);
		result.setOvertimePay(overtimePay);
		result.setLeaveDeduction(leaveDeduction);
		return result;
	}

	private BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private BigDecimal resolveTax(BigDecimal taxable, BigDecimal taxRate, PayrollRule rule) {
		if (taxRate != null) {
			return taxable.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
		}
		if (rule != null) {
			List<PayrollTaxBracket> brackets = payrollTaxBracketMapper.findByRuleId(rule.getId());
			if (brackets != null) {
				for (PayrollTaxBracket bracket : brackets) {
					BigDecimal min = bracket.getMinIncome();
					BigDecimal max = bracket.getMaxIncome();
					BigDecimal safeMin = min == null ? BigDecimal.ZERO : min;
					boolean inRange = taxable.compareTo(safeMin) >= 0 && (max == null || taxable.compareTo(max) <= 0);
					if (inRange) {
						return taxable.multiply(bracket.getRate()).setScale(2, RoundingMode.HALF_UP);
					}
				}
			}
		}
		return taxable.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
	}

	@Override
	public PayrollRecord generate(PayrollGenerateRequest request) {
		SalaryStructure structure = salaryStructureMapper.findByEmpId(request.getEmpId());
		if (structure == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Salary structure not found");
		}
		PayrollCalcRequest calcRequest = new PayrollCalcRequest();
		calcRequest.setBaseSalary(structure.getBaseSalary());
		calcRequest.setAllowance(structure.getAllowance());
		calcRequest.setBonus(structure.getBonus());
		calcRequest.setOvertimeHours(request.getOvertimeHours());
		calcRequest.setOvertimeRate(request.getOvertimeRate());
		calcRequest.setLeaveDays(request.getLeaveDays());
		calcRequest.setLeaveDeductionPerDay(request.getLeaveDeductionPerDay());
		calcRequest.setSocialSecurity(structure.getSocialSecurity());
		calcRequest.setHousingFund(structure.getHousingFund());
		calcRequest.setTaxRate(structure.getTaxRate());

		PayrollCalcResult calcResult = calculate(calcRequest);
		PayrollRecord record = new PayrollRecord();
		record.setEmpId(request.getEmpId());
		record.setCycleMonth(request.getCycleMonth());
		record.setGrossSalary(calcResult.getGrossSalary());
		record.setNetSalary(calcResult.getNetSalary());
		record.setTax(calcResult.getTax());
		record.setOvertimePay(calcResult.getOvertimePay());
		record.setLeaveDeduction(calcResult.getLeaveDeduction());
		record.setStatus("CALCULATED");
		payrollRecordMapper.insert(record);
		return record;
	}

	@Override
	public List<PayrollRecord> listByEmp(Long empId) {
		return payrollRecordMapper.findByEmpId(empId);
	}
}
