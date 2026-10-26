package com.hrm.service;

import com.hrm.auth.AuthPermissionService;
import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.Department;
import com.hrm.domain.Employee;
import com.hrm.domain.SysUser;
import com.hrm.mapper.DepartmentMapper;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.SysUserMapper;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
/** 封装当前登录用户、员工数据范围和审批分派规则。 */
public class CurrentUserService {

	public enum AccessScope {
		SELF, DEPT, ALL
	}

	private final SysUserMapper sysUserMapper;
	private final EmployeeMapper employeeMapper;
	private final DepartmentMapper departmentMapper;
	private final AuthPermissionService authPermissionService;

	public CurrentUserService(SysUserMapper sysUserMapper,
							  EmployeeMapper employeeMapper,
							  DepartmentMapper departmentMapper,
							  AuthPermissionService authPermissionService) {
		this.sysUserMapper = sysUserMapper;
		this.employeeMapper = employeeMapper;
		this.departmentMapper = departmentMapper;
		this.authPermissionService = authPermissionService;
	}

	public SysUser requireCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
			throw new BizException(ErrorCode.FORBIDDEN, "请先登录");
		}
		SysUser user = sysUserMapper.findByUsername(String.valueOf(auth.getPrincipal()));
		if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号不可用");
		}
		return user;
	}

	public Employee currentEmployeeOrNull() {
		SysUser user = requireCurrentUser();
		return user.getEmpId() == null ? null : employeeMapper.findById(user.getEmpId());
	}

	public Long currentEmpIdOrNull() {
		SysUser user = requireCurrentUser();
		return user.getEmpId();
	}

	public List<String> currentPermissions() {
		SysUser user = requireCurrentUser();
		return authPermissionService.permissionsForRole(user.getRole());
	}

	public boolean hasPermission(String permission) {
		return currentPermissions().contains(permission);
	}

	public boolean canManageAllPeople() {
		return hasPermission("hr:manage") || hasPermission("payroll:manage") || hasPermission("report:view");
	}

	public AccessScope employeeScope() {
		// 管理类权限访问全部员工，部门负责人访问本部门，普通员工只能访问本人。
		if (hasPermission("hr:manage") || hasPermission("report:view") || hasPermission("payroll:manage")) {
			return AccessScope.ALL;
		}
		Employee employee = currentEmployeeOrNull();
		if (employee != null && isDepartmentManager(employee.getId())) {
			return AccessScope.DEPT;
		}
		return AccessScope.SELF;
	}

	public AccessScope approvalScope() {
		return hasPermission("approval:manage") ? AccessScope.ALL : AccessScope.SELF;
	}

	public boolean canAccessEmployee(Long empId) {
		if (empId == null) {
			return false;
		}
		AccessScope scope = employeeScope();
		if (scope == AccessScope.ALL) {
			return true;
		}
		Long selfEmpId = currentEmpIdOrNull();
		if (scope == AccessScope.SELF) {
			return empId.equals(selfEmpId);
		}
		Employee target = employeeMapper.findById(empId);
		Employee self = selfEmpId == null ? null : employeeMapper.findById(selfEmpId);
		return target != null && self != null && target.getDeptId() != null && target.getDeptId().equals(self.getDeptId());
	}

	public boolean canAccessEmployeeRecord(Long empId) {
		return canAccessEmployee(empId) || hasPermission("approval:manage");
	}

	public Employee requireAccessibleEmployee(Long empId) {
		Employee employee = employeeMapper.findById(empId);
		if (employee == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "员工不存在");
		}
		if (!canAccessEmployee(empId) && !hasPermission("approval:manage")) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权访问该员工数据");
		}
		return employee;
	}

	public Long employeeDeptId(Long empId) {
		Employee employee = employeeMapper.findById(empId);
		return employee == null ? null : employee.getDeptId();
	}

	public boolean isDepartmentManager(Long empId) {
		if (empId == null) {
			return false;
		}
		for (Department department : departmentMapper.findAll()) {
			if (empId.equals(department.getManagerId())) {
				return true;
			}
		}
		return false;
	}

	public Long departmentManagerUserId(Long deptId) {
		if (deptId == null) {
			return null;
		}
		Department department = departmentMapper.findById(deptId);
		if (department == null || department.getManagerId() == null) {
			return null;
		}
		SysUser managerUser = sysUserMapper.findByEmpId(department.getManagerId());
		if (managerUser == null || !"ACTIVE".equalsIgnoreCase(managerUser.getStatus())) {
			return null;
		}
		return managerUser.getId();
	}

	public Long assigneeForEmployee(Long empId) {
		// 优先分派给有审批权限的部门负责人，否则回退到首个审批管理员。
		Long managerUserId = departmentManagerUserId(employeeDeptId(empId));
		if (managerUserId != null) {
			SysUser managerUser = sysUserMapper.findById(managerUserId);
			if (managerUser != null
					&& "ACTIVE".equalsIgnoreCase(managerUser.getStatus())
					&& authPermissionService.permissionsForRole(managerUser.getRole()).contains("approval:manage")) {
				return managerUserId;
			}
		}
		return firstApprovalManagerUserId();
	}

	public Long assigneeForManagerWorkflow() {
		return firstApprovalManagerUserId();
	}

	public Long firstApprovalManagerUserId() {
		for (SysUser user : sysUserMapper.findAll()) {
			if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
				continue;
			}
			List<String> perms = authPermissionService.permissionsForRole(user.getRole());
			if (perms.contains("approval:manage")) {
				return user.getId();
			}
		}
		return null;
	}
}
