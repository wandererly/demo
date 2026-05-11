package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.Department;
import com.hrm.dto.DepartmentCreateRequest;
import com.hrm.dto.DepartmentUpdateRequest;
import com.hrm.mapper.DepartmentMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.DepartmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	private final DepartmentMapper departmentMapper;
	private final AuditLogService auditLogService;

	public DepartmentServiceImpl(DepartmentMapper departmentMapper, AuditLogService auditLogService) {
		this.departmentMapper = departmentMapper;
		this.auditLogService = auditLogService;
	}

	@Override
	public List<Department> list() {
		return departmentMapper.findAll();
	}

	@Override
	public Department get(Long id) {
		Department department = departmentMapper.findById(id);
		if (department == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Department not found");
		}
		return department;
	}

	@Override
	public Department create(DepartmentCreateRequest request) {
		Department department = new Department();
		department.setName(request.getName());
		department.setParentId(request.getParentId());
		department.setManagerId(request.getManagerId());
		department.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
		departmentMapper.insert(department);
		auditLogService.record("部门", "新增", "department", department.getId(), department.getName());
		return department;
	}

	@Override
	public Department update(Long id, DepartmentUpdateRequest request) {
		Department existing = get(id);
		existing.setName(request.getName() == null ? existing.getName() : request.getName());
		existing.setParentId(request.getParentId() == null ? existing.getParentId() : request.getParentId());
		existing.setManagerId(request.getManagerId() == null ? existing.getManagerId() : request.getManagerId());
		existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
		departmentMapper.update(existing);
		auditLogService.record("部门", "更新", "department", existing.getId(), existing.getName());
		return existing;
	}

	@Override
	public void delete(Long id) {
		int rows = departmentMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Department not found");
		}
		auditLogService.record("部门", "删除", "department", id, null);
	}
}
