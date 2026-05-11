package com.hrm.service;

import com.hrm.domain.Employee;
import com.hrm.domain.SysUser;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.SysUserMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAccountService {

	private static final String EMP_NO_PATTERN = "^E\\d+\\d{3}$";

	private final EmployeeMapper employeeMapper;
	private final SysUserMapper sysUserMapper;
	private final PasswordEncoder passwordEncoder;

	public EmployeeAccountService(EmployeeMapper employeeMapper, SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
		this.employeeMapper = employeeMapper;
		this.sysUserMapper = sysUserMapper;
		this.passwordEncoder = passwordEncoder;
	}

	public SysUser ensureEmployeeAccount(Employee employee) {
		if (employee == null || employee.getId() == null) {
			return null;
		}
		normalizeEmployeeNumber(employee);
		if (employee.getEmpNo() == null || employee.getEmpNo().isBlank()) {
			return null;
		}
		String username = accountUsername(employee);
		SysUser existing = sysUserMapper.findByUsername(username);
		if (existing == null) {
			SysUser boundUser = sysUserMapper.findByEmpId(employee.getId());
			if (shouldRenameBoundUser(boundUser, username)) {
				boundUser.setUsername(username);
				if (boundUser.getRole() == null || boundUser.getRole().isBlank()) {
					boundUser.setRole("EMPLOYEE");
				}
				if (boundUser.getStatus() == null || boundUser.getStatus().isBlank()) {
					boundUser.setStatus("ACTIVE");
				}
				sysUserMapper.update(boundUser);
				return boundUser;
			}
			SysUser user = new SysUser();
			user.setUsername(username);
			user.setPasswordHash(passwordEncoder.encode(initialPassword(employee)));
			user.setRole("EMPLOYEE");
			user.setEmpId(employee.getId());
			user.setStatus("ACTIVE");
			sysUserMapper.insert(user);
			return user;
		}
		boolean changed = false;
		if (existing.getEmpId() == null) {
			existing.setEmpId(employee.getId());
			changed = true;
		}
		if (existing.getRole() == null || existing.getRole().isBlank()) {
			existing.setRole("EMPLOYEE");
			changed = true;
		}
		if (existing.getStatus() == null || existing.getStatus().isBlank()) {
			existing.setStatus("ACTIVE");
			changed = true;
		}
		if (changed) {
			sysUserMapper.update(existing);
		}
		return existing;
	}

	public void normalizeEmployeeNumber(Employee employee) {
		if (employee == null || employee.getDeptId() == null) {
			return;
		}
		String empNo = employee.getEmpNo();
		if (isValidDepartmentEmployeeNumber(empNo, employee.getDeptId())) {
			return;
		}
		String nextEmpNo = nextEmployeeNumber(employee.getDeptId(), employee.getId());
		employee.setEmpNo(nextEmpNo);
		if (employee.getId() != null) {
			employeeMapper.updateEmpNo(employee.getId(), nextEmpNo);
		}
	}

	public String accountUsername(Employee employee) {
		return employee.getEmpNo().trim().toLowerCase(Locale.ROOT);
	}

	public String initialPassword(Employee employee) {
		return employee.getEmpNo().trim().toUpperCase() + "@123";
	}

	private boolean shouldRenameBoundUser(SysUser boundUser, String username) {
		return boundUser != null
				&& !username.equalsIgnoreCase(boundUser.getUsername())
				&& (isInvalidUsername(boundUser.getUsername()) || "EMPLOYEE".equalsIgnoreCase(boundUser.getRole()));
	}

	private boolean isInvalidUsername(String username) {
		if (username == null || username.isBlank()) {
			return true;
		}
		String normalized = username.trim().toUpperCase(Locale.ROOT);
		return normalized.matches("\\d+") || !normalized.matches(EMP_NO_PATTERN);
	}

	private boolean isValidDepartmentEmployeeNumber(String empNo, Long deptId) {
		if (empNo == null || empNo.isBlank() || deptId == null) {
			return false;
		}
		String normalized = empNo.trim().toUpperCase(Locale.ROOT);
		return normalized.matches("^E" + deptId + "\\d{3}$");
	}

	private String nextEmployeeNumber(Long deptId, Long currentEmployeeId) {
		int max = 0;
		List<Employee> employees = employeeMapper.findByDeptId(deptId);
		for (Employee employee : employees) {
			if (employee == null || employee.getEmpNo() == null) {
				continue;
			}
			String empNo = employee.getEmpNo().trim().toUpperCase(Locale.ROOT);
			if (!empNo.matches("^E" + deptId + "\\d{3}$")) {
				continue;
			}
			String suffix = empNo.substring(("E" + deptId).length());
			try {
				max = Math.max(max, Integer.parseInt(suffix));
			} catch (NumberFormatException ignored) {
				// Skip malformed historical employee numbers.
			}
		}
		for (int next = max + 1; next < 1000; next += 1) {
			String candidate = String.format("E%s%03d", deptId, next);
			String username = candidate.toLowerCase(Locale.ROOT);
			SysUser user = sysUserMapper.findByUsername(username);
			if (user == null || (currentEmployeeId != null && currentEmployeeId.equals(user.getEmpId()))) {
				return candidate;
			}
		}
		throw new IllegalStateException("部门员工账号序列已满");
	}
}
