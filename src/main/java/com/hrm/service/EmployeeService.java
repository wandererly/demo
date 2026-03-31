package com.hrm.service;

import com.hrm.domain.Employee;
import com.hrm.dto.EmployeeCreateRequest;
import com.hrm.dto.EmployeeUpdateRequest;
import java.util.List;

public interface EmployeeService {
	List<Employee> list();
	Employee get(Long id);
	Employee create(EmployeeCreateRequest request);
	Employee update(Long id, EmployeeUpdateRequest request);
	void delete(Long id);
}
