package com.hrm.service;

import com.hrm.domain.PayrollRecord;
import com.hrm.dto.PayrollCalcRequest;
import com.hrm.dto.PayrollCalcResult;
import com.hrm.dto.PayrollGenerateRequest;
import java.util.List;

public interface PayrollService {
	PayrollCalcResult calculate(PayrollCalcRequest request);
	PayrollRecord generate(PayrollGenerateRequest request);
	List<PayrollRecord> list();
	List<PayrollRecord> listByEmp(Long empId);
}
