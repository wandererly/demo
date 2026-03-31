package com.hrm.service;

import com.hrm.domain.SalaryStructure;
import com.hrm.dto.SalaryStructureUpsertRequest;

public interface SalaryStructureService {
	SalaryStructure getByEmpId(Long empId);
	SalaryStructure upsert(SalaryStructureUpsertRequest request);
}
