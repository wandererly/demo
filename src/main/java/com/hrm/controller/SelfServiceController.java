package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.Department;
import com.hrm.domain.Employee;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.Payslip;
import com.hrm.domain.PerformanceGoal;
import com.hrm.domain.SysUser;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.mapper.DepartmentMapper;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.EnterpriseMapper;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.SysUserMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/self")
@PreAuthorize("isAuthenticated()")
public class SelfServiceController {

	private final SysUserMapper sysUserMapper;
	private final EmployeeMapper employeeMapper;
	private final DepartmentMapper departmentMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final EnterpriseMapper enterpriseMapper;

	public SelfServiceController(SysUserMapper sysUserMapper,
								 EmployeeMapper employeeMapper,
								 DepartmentMapper departmentMapper,
								 AttendanceRecordMapper attendanceRecordMapper,
								 LeaveRequestMapper leaveRequestMapper,
								 EnterpriseMapper enterpriseMapper) {
		this.sysUserMapper = sysUserMapper;
		this.employeeMapper = employeeMapper;
		this.departmentMapper = departmentMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.leaveRequestMapper = leaveRequestMapper;
		this.enterpriseMapper = enterpriseMapper;
	}

	@GetMapping("/profile")
	public ApiResponse<Employee> profile() {
		return ApiResponse.ok(currentEmployee());
	}

	@GetMapping("/attendance")
	public ApiResponse<List<AttendanceRecord>> attendance() {
		return ApiResponse.ok(attendanceRecordMapper.findByEmpId(currentEmpId()));
	}

	@GetMapping("/leave")
	public ApiResponse<List<LeaveRequest>> leave() {
		return ApiResponse.ok(leaveRequestMapper.findByEmpId(currentEmpId()));
	}

	@GetMapping("/performance-goals")
	public ApiResponse<List<PerformanceGoal>> performanceGoals() {
		return ApiResponse.ok(enterpriseMapper.listPerformanceGoalsByEmpId(currentEmpId()));
	}

	@GetMapping("/payslips")
	public ApiResponse<List<Payslip>> payslips() {
		return ApiResponse.ok(enterpriseMapper.listPayslipsByEmpId(currentEmpId()));
	}

	@GetMapping("/notifications")
	public ApiResponse<List<NotificationItem>> notifications() {
		SysUser user = currentUser();
		return ApiResponse.ok(enterpriseMapper.listNotificationsForUser(user.getEmpId(), user.getUsername()));
	}

	@PostMapping("/notifications/{id}/read")
	public ApiResponse<Void> markNotificationRead(@PathVariable Long id) {
		NotificationItem target = enterpriseMapper.listNotificationsForUser(currentEmpId(), currentUser().getUsername())
				.stream()
				.filter(item -> id.equals(item.getId()))
				.findFirst()
				.orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "通知不存在或无权操作"));
		enterpriseMapper.markNotificationRead(target.getId());
		return ApiResponse.ok(null);
	}

	@GetMapping("/team/employees")
	public ApiResponse<List<Employee>> teamEmployees() {
		Long empId = currentEmpId();
		List<Employee> rows = new ArrayList<>();
		for (Department department : departmentMapper.findAll()) {
			if (empId.equals(department.getManagerId())) {
				rows.addAll(employeeMapper.findByDeptId(department.getId()));
			}
		}
		return ApiResponse.ok(rows);
	}

	private Employee currentEmployee() {
		Employee employee = employeeMapper.findById(currentEmpId());
		if (employee == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "当前账号未绑定有效员工档案");
		}
		return employee;
	}

	private Long currentEmpId() {
		SysUser user = currentUser();
		if (user.getEmpId() == null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "当前账号未绑定员工档案，无法使用员工自助");
		}
		return user.getEmpId();
	}

	private SysUser currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			throw new BizException(ErrorCode.FORBIDDEN, "请先登录");
		}
		SysUser user = sysUserMapper.findByUsername(String.valueOf(auth.getPrincipal()));
		if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号不可用");
		}
		return user;
	}
}
