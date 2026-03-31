package com.hrm.service.impl;

import com.hrm.domain.SalaryStructure;
import com.hrm.dto.SalaryStructureUpsertRequest;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.service.SalaryStructureService;
import org.springframework.stereotype.Service;

@Service
public class SalaryStructureServiceImpl implements SalaryStructureService {

	private final SalaryStructureMapper salaryStructureMapper;

	public SalaryStructureServiceImpl(SalaryStructureMapper salaryStructureMapper) {
		this.salaryStructureMapper = salaryStructureMapper;
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
		return entity;
	}
}
