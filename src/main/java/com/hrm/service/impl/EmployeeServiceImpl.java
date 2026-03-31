package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.Employee;
import com.hrm.dto.EmployeeCreateRequest;
import com.hrm.dto.EmployeeUpdateRequest;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.service.EmployeeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeMapper employeeMapper;

	public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
		this.employeeMapper = employeeMapper;
	}

	@Override
	public List<Employee> list() {
		return employeeMapper.findAll();
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
		employeeMapper.insert(employee);
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
		return existing;
	}

	@Override
	public void delete(Long id) {
		int rows = employeeMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Employee not found");
		}
	}
}
