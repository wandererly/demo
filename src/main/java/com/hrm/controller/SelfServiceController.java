package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.AttendanceRuleConfig;
import com.hrm.domain.Department;
import com.hrm.domain.Employee;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.Payslip;
import com.hrm.domain.PerformanceGoal;
import com.hrm.domain.SysUser;
import com.hrm.dto.AttendanceCorrectionRequest;
import com.hrm.dto.OvertimeRequestDto;
import com.hrm.dto.LeaveCreateRequest;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.mapper.DepartmentMapper;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.EnterpriseMapper;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.service.EnterpriseService;
import com.hrm.service.LeaveService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
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
/** 员工自助接口，所有员工编号均从当前登录账号取得，避免客户端越权传参。 */
public class SelfServiceController {

	private final SysUserMapper sysUserMapper;
	private final EmployeeMapper employeeMapper;
	private final DepartmentMapper departmentMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final EnterpriseMapper enterpriseMapper;
	private final EnterpriseService enterpriseService;
	private final LeaveService leaveService;

	public SelfServiceController(SysUserMapper sysUserMapper,
								 EmployeeMapper employeeMapper,
								 DepartmentMapper departmentMapper,
								 AttendanceRecordMapper attendanceRecordMapper,
								 LeaveRequestMapper leaveRequestMapper,
								 EnterpriseMapper enterpriseMapper,
								 EnterpriseService enterpriseService,
								 LeaveService leaveService) {
		this.sysUserMapper = sysUserMapper;
		this.employeeMapper = employeeMapper;
		this.departmentMapper = departmentMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.leaveRequestMapper = leaveRequestMapper;
		this.enterpriseMapper = enterpriseMapper;
		this.enterpriseService = enterpriseService;
		this.leaveService = leaveService;
	}

	@GetMapping("/profile")
	public ApiResponse<Employee> profile() {
		return ApiResponse.ok(currentEmployee());
	}

	@GetMapping("/contracts")
	public ApiResponse<List<EmployeeContract>> contracts() {
		return ApiResponse.ok(enterpriseMapper.listContractsByEmpId(currentEmpId()));
	}

	@GetMapping("/attendance")
	public ApiResponse<List<AttendanceRecord>> attendance() {
		return ApiResponse.ok(attendanceRecordMapper.findByEmpId(currentEmpId()));
	}

	@GetMapping("/attendance/today")
	public ApiResponse<AttendanceRecord> todayAttendance() {
		return ApiResponse.ok(todayRecord(currentEmpId(), false));
	}

	@PostMapping("/attendance/clock-in")
	public ApiResponse<AttendanceRecord> clockIn() {
		// 同一员工同一天只有一条考勤记录，重复上班打卡直接拒绝。
		Long empId = currentEmpId();
		AttendanceRecord record = todayRecord(empId, true);
		if (record.getCheckIn() != null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "今天已经完成上班打卡");
		}
		LocalDateTime now = LocalDateTime.now();
		AttendanceRuleConfig config = enterpriseService.getAttendanceRuleConfig();
		record.setCheckIn(now);
		LocalTime startTime = parseTime(config.getWorkStartTime(), LocalTime.of(9, 0)).plusMinutes(config.getLateGraceMinutes() == null ? 0 : config.getLateGraceMinutes());
		record.setStatus(now.toLocalTime().isAfter(startTime) ? "LATE" : "NORMAL");
		attendanceRecordMapper.update(record);
		return ApiResponse.ok(record);
	}

	@PostMapping("/attendance/clock-out")
	public ApiResponse<AttendanceRecord> clockOut() {
		// 下班打卡必须建立在已上班打卡且尚未签退的基础上。
		Long empId = currentEmpId();
		AttendanceRecord record = todayRecord(empId, true);
		if (record.getCheckIn() == null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请先完成上班打卡");
		}
		if (record.getCheckOut() != null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "今天已经完成下班打卡");
		}
		LocalDateTime now = LocalDateTime.now();
		AttendanceRuleConfig config = enterpriseService.getAttendanceRuleConfig();
		record.setCheckOut(now);
		LocalTime endTime = parseTime(config.getWorkEndTime(), LocalTime.of(18, 0)).minusMinutes(config.getEarlyLeaveGraceMinutes() == null ? 0 : config.getEarlyLeaveGraceMinutes());
		if (!"LATE".equals(record.getStatus()) && now.toLocalTime().isBefore(endTime)) {
			record.setStatus("EARLY_LEAVE");
		} else if ("ABSENT".equals(record.getStatus()) || record.getStatus() == null || record.getStatus().isBlank()) {
			record.setStatus("NORMAL");
		}
		attendanceRecordMapper.update(record);
		return ApiResponse.ok(record);
	}

	@PostMapping("/attendance/corrections")
	public ApiResponse<?> submitSelfCorrection(@RequestBody AttendanceCorrectionRequest request) {
		request.setEmpId(currentEmpId());
		return ApiResponse.ok(enterpriseService.submitCorrection(request));
	}

	@PostMapping("/attendance/overtime")
	public ApiResponse<?> submitSelfOvertime(@RequestBody OvertimeRequestDto request) {
		request.setEmpId(currentEmpId());
		return ApiResponse.ok(enterpriseService.submitOvertime(request));
	}

	@PostMapping("/leave")
	public ApiResponse<LeaveRequest> submitSelfLeave(@RequestBody LeaveCreateRequest request) {
		request.setEmpId(currentEmpId());
		return ApiResponse.ok(leaveService.create(request));
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

	@PostMapping("/payslips/{id}/acknowledge")
	public ApiResponse<Payslip> acknowledgePayslip(@PathVariable Long id) {
		return ApiResponse.ok(enterpriseService.acknowledgePayslip(id));
	}

	@GetMapping("/payslips/{id}/export")
	public ResponseEntity<byte[]> exportPayslip(@PathVariable Long id) {
		byte[] body = enterpriseService.exportPayslip(id).getBytes(java.nio.charset.StandardCharsets.UTF_8);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename("payslip-" + id + ".csv", java.nio.charset.StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
				.body(body);
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

	private AttendanceRecord todayRecord(Long empId, boolean createIfMissing) {
		// 首次操作当天考勤时创建 ABSENT 基线，后续打卡只更新这条记录。
		AttendanceRecord record = attendanceRecordMapper.findByEmpIdAndWorkDate(empId, LocalDate.now());
		if (record != null || !createIfMissing) {
			return record;
		}
		record = new AttendanceRecord();
		record.setEmpId(empId);
		record.setWorkDate(LocalDate.now());
		record.setStatus("ABSENT");
		attendanceRecordMapper.insert(record);
		return record;
	}

	private LocalTime parseTime(String value, LocalTime fallback) {
		try {
			return value == null || value.isBlank() ? fallback : LocalTime.parse(value);
		} catch (Exception ex) {
			return fallback;
		}
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
