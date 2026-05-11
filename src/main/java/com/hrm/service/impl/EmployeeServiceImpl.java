package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.Employee;
import com.hrm.domain.SysUser;
import com.hrm.dto.EmployeeCreateRequest;
import com.hrm.dto.EmployeeUpdateRequest;
import com.hrm.dto.EmployeeView;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.EmployeeAccountService;
import com.hrm.service.EmployeeService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeMapper employeeMapper;
	private final SysUserMapper sysUserMapper;
	private final EmployeeAccountService employeeAccountService;
	private final AuditLogService auditLogService;

	public EmployeeServiceImpl(EmployeeMapper employeeMapper,
							   SysUserMapper sysUserMapper,
							   EmployeeAccountService employeeAccountService,
							   AuditLogService auditLogService) {
		this.employeeMapper = employeeMapper;
		this.sysUserMapper = sysUserMapper;
		this.employeeAccountService = employeeAccountService;
		this.auditLogService = auditLogService;
	}

	@Override
	public List<Employee> list() {
		return employeeMapper.findAll();
	}

	@Override
	public List<EmployeeView> listViews() {
		List<EmployeeView> views = new ArrayList<>();
		for (Employee employee : employeeMapper.findAll()) {
			SysUser user = employee.getEmpNo() == null ? null : sysUserMapper.findByUsername(employeeAccountService.accountUsername(employee));
			if (user == null && employee.getId() != null) {
				user = sysUserMapper.findByEmpId(employee.getId());
			}
			views.add(EmployeeView.from(employee, user == null ? null : user.getUsername()));
		}
		return views;
	}

	@Override
	public Employee get(Long id) {
		Employee employee = employeeMapper.findById(id);
		if (employee == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Employee not found");
		}
		return employee;
	}

	@Override
	public Employee create(EmployeeCreateRequest request) {
		Employee employee = new Employee();
		employee.setEmpNo(request.getEmpNo());
		employee.setName(request.getName());
		employee.setGender(request.getGender());
		employee.setPhone(request.getPhone());
		employee.setEmail(request.getEmail());
		employee.setDeptId(request.getDeptId());
		employee.setPosition(request.getPosition());
		employee.setHireDate(request.getHireDate());
		employee.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
		employee.setBaseSalary(request.getBaseSalary());
		employeeAccountService.normalizeEmployeeNumber(employee);
		employeeMapper.insert(employee);
		employeeAccountService.ensureEmployeeAccount(employee);
		auditLogService.record("员工", "新增", "employee", employee.getId(), employee.getName());
		return employee;
	}

	@Override
	public Employee update(Long id, EmployeeUpdateRequest request) {
		Employee existing = get(id);
		existing.setName(request.getName() == null ? existing.getName() : request.getName());
		existing.setGender(request.getGender() == null ? existing.getGender() : request.getGender());
		existing.setPhone(request.getPhone() == null ? existing.getPhone() : request.getPhone());
		existing.setEmail(request.getEmail() == null ? existing.getEmail() : request.getEmail());
		existing.setDeptId(request.getDeptId() == null ? existing.getDeptId() : request.getDeptId());
		existing.setPosition(request.getPosition() == null ? existing.getPosition() : request.getPosition());
		existing.setHireDate(request.getHireDate() == null ? existing.getHireDate() : request.getHireDate());
		existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
		existing.setBaseSalary(request.getBaseSalary() == null ? existing.getBaseSalary() : request.getBaseSalary());
		employeeMapper.update(existing);
		auditLogService.record("员工", "更新", "employee", existing.getId(), existing.getName());
		return existing;
	}

	@Override
	public void delete(Long id) {
		int rows = employeeMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Employee not found");
		}
		auditLogService.record("员工", "删除", "employee", id, null);
	}
}
