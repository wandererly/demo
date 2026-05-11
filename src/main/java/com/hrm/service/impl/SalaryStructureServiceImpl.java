package com.hrm.service.impl;

import com.hrm.domain.SalaryStructure;
import com.hrm.dto.SalaryStructureUpsertRequest;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.SalaryStructureService;
import org.springframework.stereotype.Service;

@Service
public class SalaryStructureServiceImpl implements SalaryStructureService {

	private final SalaryStructureMapper salaryStructureMapper;
	private final AuditLogService auditLogService;

	public SalaryStructureServiceImpl(SalaryStructureMapper salaryStructureMapper, AuditLogService auditLogService) {
		this.salaryStructureMapper = salaryStructureMapper;
		this.auditLogService = auditLogService;
	}

	@Override
	public SalaryStructure getByEmpId(Long empId) {
		return salaryStructureMapper.findByEmpId(empId);
	}

	@Override
	public SalaryStructure upsert(SalaryStructureUpsertRequest request) {
		SalaryStructure existing = salaryStructureMapper.findByEmpId(request.getEmpId());
		SalaryStructure entity = new SalaryStructure();
		entity.setEmpId(request.getEmpId());
		entity.setBaseSalary(request.getBaseSalary());
		entity.setAllowance(request.getAllowance());
		entity.setBonus(request.getBonus());
		entity.setSocialSecurity(request.getSocialSecurity());
		entity.setHousingFund(request.getHousingFund());
		entity.setTaxRate(request.getTaxRate());
		if (existing == null) {
			salaryStructureMapper.insert(entity);
		} else {
			salaryStructureMapper.updateByEmpId(entity);
			entity.setId(existing.getId());
		}
		auditLogService.record("薪酬", existing == null ? "新增薪资结构" : "更新薪资结构",
				"salary_structure", entity.getId(), "员工ID " + entity.getEmpId());
		return entity;
	}
}
