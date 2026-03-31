package com.hrm.service;

import com.hrm.domain.Department;
import com.hrm.dto.DepartmentCreateRequest;
import com.hrm.dto.DepartmentUpdateRequest;
import java.util.List;

public interface DepartmentService {
	List<Department> list();
	Department get(Long id);
	Department create(DepartmentCreateRequest request);
	Department update(Long id, DepartmentUpdateRequest request);
	void delete(Long id);
}
