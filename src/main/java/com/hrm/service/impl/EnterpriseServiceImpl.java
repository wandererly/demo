package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.AttendanceMonthLock;
import com.hrm.domain.AttendanceMonthlySummary;
import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.AttendanceRuleConfig;
import com.hrm.domain.DepartmentShiftBinding;
import com.hrm.domain.Employee;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.EmployeeShiftBinding;
import com.hrm.domain.Holiday;
import com.hrm.domain.LeaveBalance;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.OvertimeRequest;
import com.hrm.domain.Payslip;
import com.hrm.domain.PayslipComponent;
import com.hrm.domain.PerformanceGoal;
import com.hrm.domain.SalaryStructure;
import com.hrm.domain.SalaryChange;
import com.hrm.domain.WorkShift;
import com.hrm.dto.AttendanceCorrectionRequest;
import com.hrm.dto.AttendanceSummaryRequest;
import com.hrm.dto.ContractRequest;
import com.hrm.dto.HolidayRequest;
import com.hrm.dto.LeaveBalanceRequest;
import com.hrm.dto.LifecycleEventRequest;
import com.hrm.dto.OvertimeRequestDto;
import com.hrm.dto.PayslipGenerateRequest;
import com.hrm.dto.PerformanceGoalRequest;
import com.hrm.dto.PerformanceGoalScoreRequest;
import com.hrm.dto.SalaryChangeRequest;
import com.hrm.dto.ShiftRequest;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.EnterpriseMapper;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.CurrentUserService;
import com.hrm.service.EnterpriseService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** 企业人事扩展业务：考勤、审批联动、假期余额、薪酬和通知。 */
public class EnterpriseServiceImpl implements EnterpriseService {

	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

	private final EnterpriseMapper enterpriseMapper;
	private final EmployeeMapper employeeMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final SalaryStructureMapper salaryStructureMapper;
	private final AuditLogService auditLogService;
	private final CurrentUserService currentUserService;

	public EnterpriseServiceImpl(EnterpriseMapper enterpriseMapper,
								 EmployeeMapper employeeMapper,
								 AttendanceRecordMapper attendanceRecordMapper,
								 LeaveRequestMapper leaveRequestMapper,
								 SalaryStructureMapper salaryStructureMapper,
								 AuditLogService auditLogService,
								 CurrentUserService currentUserService) {
		this.enterpriseMapper = enterpriseMapper;
		this.employeeMapper = employeeMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.leaveRequestMapper = leaveRequestMapper;
		this.salaryStructureMapper = salaryStructureMapper;
		this.auditLogService = auditLogService;
		this.currentUserService = currentUserService;
	}

	@Override
	public List<EmployeeContract> listContracts() {
		List<EmployeeContract> rows = new ArrayList<>();
		for (EmployeeContract contract : enterpriseMapper.listContracts()) {
			if (currentUserService.canAccessEmployeeRecord(contract.getEmpId())) {
				rows.add(contract);
			}
		}
		return rows;
	}

	@Override
	@Transactional
	public EmployeeContract createContract(ContractRequest request) {
		Employee employee = requireEmployee(request.getEmpId());
		EmployeeContract contract = new EmployeeContract();
		contract.setEmpId(request.getEmpId());
		contract.setContractNo(request.getContractNo());
		contract.setContractType(request.getContractType());
		contract.setStartDate(request.getStartDate());
		contract.setEndDate(request.getEndDate());
		contract.setStatus(blankToDefault(request.getStatus(), "ACTIVE"));
		contract.setProbationStartDate(request.getProbationStartDate());
		contract.setProbationEndDate(request.getProbationEndDate());
		contract.setRenewalStatus("PENDING");
		contract.setReminderSent(false);
		enterpriseMapper.insertContract(contract);
		employee.setProbationStartDate(request.getProbationStartDate());
		employee.setProbationEndDate(request.getProbationEndDate());
		employee.setRegularizationDueDate(request.getProbationEndDate());
		employeeMapper.update(employee);
		LifecycleEvent event = new LifecycleEvent();
		event.setEmpId(employee.getId());
		event.setEventType("CONTRACT");
		event.setTitle("签订合同 " + contract.getContractNo());
		event.setFromDeptId(employee.getDeptId());
		event.setFromPosition(employee.getPosition());
		event.setEffectiveDate(contract.getStartDate());
		event.setStatus("APPROVED");
		event.setDetail(contract.getContractType());
		enterpriseMapper.insertLifecycleEvent(event);
		String contractTypeName = switch (contract.getContractType()) {
			case "FIXED" -> "固定期限劳动合同";
			case "UNFIXED" -> "无固定期限劳动合同";
			case "INTERNSHIP" -> "实习协议";
			default -> "劳动合同";
		};
		notifyEmployee(employee.getId(), "劳动合同已登记",
				"您有一份" + contractTypeName + "已登记，合同编号：" + contract.getContractNo()
						+ "，开始日期：" + contract.getStartDate()
						+ (contract.getEndDate() == null ? "" : "，结束日期：" + contract.getEndDate()),
				"CONTRACT", contract.getId());
		auditLogService.record("员工生命周期", "合同", "employee_contract", contract.getId(), employee.getName());
		return contract;
	}

	@Override
	public List<LifecycleEvent> listLifecycleEvents() {
		List<LifecycleEvent> rows = new ArrayList<>();
		for (LifecycleEvent event : enterpriseMapper.listLifecycleEvents()) {
			if (currentUserService.canAccessEmployeeRecord(event.getEmpId())) {
				rows.add(event);
			}
		}
		return rows;
	}

	@Override
	@Transactional
	public LifecycleEvent submitLifecycleEvent(LifecycleEventRequest request) {
		Employee employee = requireEmployee(request.getEmpId());
		String type = normalizeLifecycleType(request.getEventType());
		LifecycleEvent event = new LifecycleEvent();
		event.setEmpId(employee.getId());
		event.setEventType(type);
		event.setTitle(blankToDefault(request.getTitle(), defaultLifecycleTitle(type)));
		event.setFromDeptId(employee.getDeptId());
		event.setToDeptId(request.getToDeptId());
		event.setFromPosition(employee.getPosition());
		event.setToPosition(request.getToPosition());
		event.setEffectiveDate(request.getEffectiveDate());
		event.setStatus("PENDING");
		event.setAssigneeUserId(assigneeForLifecycle(employee, type));
		event.setTargetSalary(request.getTargetSalary());
		event.setAccountAction("RESIGNATION".equals(type) ? "LOCK" : "KEEP");
		event.setAssetHandoverStatus("RESIGNATION".equals(type) ? "PENDING" : "NOT_REQUIRED");
		event.setDetail(request.getDetail());
		enterpriseMapper.insertLifecycleEvent(event);

		employee.setStatus(pendingEmployeeStatus(type));
		employeeMapper.update(employee);
		notifyEmployee(employee.getId(), "生命周期审批待办", event.getTitle() + " 已提交审批");
		auditLogService.record("员工生命周期", "提交", "employee_lifecycle_event", event.getId(), event.getTitle());
		return event;
	}

	@Override
	@Transactional
	public void approveLifecycle(Long id, Long approverId, String status) {
		LifecycleEvent event = requireLifecycle(id);
		String decision = normalizeDecision(status);
		if (isFinalStatus(event.getStatus())) {
			return;
		}
		Employee employee = requireEmployee(event.getEmpId());
		String before = employeeState(employee);
		event.setStatus(decision);
		event.setApproverId(approverId);
		enterpriseMapper.updateLifecycleStatus(event);
		if ("APPROVED".equals(decision)) {
			applyLifecycle(employee, event);
		} else if ("REJECTED".equals(decision)) {
			employee.setStatus("ACTIVE");
			employeeMapper.update(employee);
		}
		Employee afterEmployee = requireEmployee(employee.getId());
		notifyEmployee(employee.getId(), "生命周期审批结果", event.getTitle() + "：" + decision);
		auditLogService.recordChange("员工生命周期", "审批", "employee_lifecycle_event", event.getId(),
				before, employeeState(afterEmployee), decision);
	}

	@Override
	public List<WorkShift> listShifts() {
		return enterpriseMapper.listShifts();
	}

	@Override
	public WorkShift createShift(ShiftRequest request) {
		WorkShift shift = new WorkShift();
		shift.setShiftName(request.getShiftName());
		shift.setStartTime(request.getStartTime());
		shift.setEndTime(request.getEndTime());
		shift.setWorkHours(request.getWorkHours());
		shift.setStatus(blankToDefault(request.getStatus(), "ACTIVE"));
		enterpriseMapper.insertShift(shift);
		auditLogService.record("考勤规则", "新增班次", "work_shift", shift.getId(), shift.getShiftName());
		return shift;
	}

	@Override
	public AttendanceRuleConfig getAttendanceRuleConfig() {
		AttendanceRuleConfig config = enterpriseMapper.findAttendanceRuleConfig();
		if (config != null) {
			return config;
		}
		AttendanceRuleConfig fallback = new AttendanceRuleConfig();
		fallback.setWorkStartTime("09:00");
		fallback.setWorkEndTime("18:00");
		fallback.setLateGraceMinutes(0);
		fallback.setEarlyLeaveGraceMinutes(0);
		fallback.setAbsenceRule("NO_CHECK_IN");
		fallback.setGenerateBaselineOnHoliday(false);
		fallback.setGenerateBaselineOnWorkday(true);
		return fallback;
	}

	@Override
	public AttendanceRuleConfig saveAttendanceRuleConfig(AttendanceRuleConfig config) {
		if (config == null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "考勤规则不能为空");
		}
		config.setWorkStartTime(blankToDefault(config.getWorkStartTime(), "09:00"));
		config.setWorkEndTime(blankToDefault(config.getWorkEndTime(), "18:00"));
		config.setLateGraceMinutes(config.getLateGraceMinutes() == null ? 0 : config.getLateGraceMinutes());
		config.setEarlyLeaveGraceMinutes(config.getEarlyLeaveGraceMinutes() == null ? 0 : config.getEarlyLeaveGraceMinutes());
		try {
			LocalTime start = LocalTime.parse(config.getWorkStartTime());
			LocalTime end = LocalTime.parse(config.getWorkEndTime());
			if (!end.isAfter(start)) {
				throw new BizException(ErrorCode.BAD_REQUEST, "下班时间必须晚于上班时间");
			}
		} catch (java.time.format.DateTimeParseException ex) {
			throw new BizException(ErrorCode.BAD_REQUEST, "上下班时间格式必须为HH:mm");
		}
		if (config.getLateGraceMinutes() < 0 || config.getEarlyLeaveGraceMinutes() < 0
				|| config.getLateGraceMinutes() > 180 || config.getEarlyLeaveGraceMinutes() > 180) {
			throw new BizException(ErrorCode.BAD_REQUEST, "宽限时间必须在0到180分钟之间");
		}
		LocalTime effectiveStart = LocalTime.parse(config.getWorkStartTime()).plusMinutes(config.getLateGraceMinutes());
		LocalTime effectiveEnd = LocalTime.parse(config.getWorkEndTime()).minusMinutes(config.getEarlyLeaveGraceMinutes());
		if (!effectiveEnd.isAfter(effectiveStart)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "迟到和早退宽限时间不能覆盖整个工作时段");
		}
		config.setAbsenceRule(blankToDefault(config.getAbsenceRule(), "NO_CHECK_IN"));
		config.setGenerateBaselineOnHoliday(config.getGenerateBaselineOnHoliday() != null && config.getGenerateBaselineOnHoliday());
		config.setGenerateBaselineOnWorkday(config.getGenerateBaselineOnWorkday() == null || config.getGenerateBaselineOnWorkday());
		if (config.getId() == null) {
			enterpriseMapper.insertAttendanceRuleConfig(config);
		} else {
			enterpriseMapper.updateAttendanceRuleConfig(config);
		}
		auditLogService.record("考勤规则", "保存配置", "attendance_rule_config", config.getId(), config.getWorkStartTime() + "-" + config.getWorkEndTime());
		return getAttendanceRuleConfig();
	}

	@Override
	public List<DepartmentShiftBinding> listDepartmentShiftBindings() {
		return enterpriseMapper.listDepartmentShiftBindings();
	}

	@Override
	public DepartmentShiftBinding saveDepartmentShiftBinding(DepartmentShiftBinding binding) {
		if (binding.getDeptId() == null || binding.getShiftId() == null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请选择部门和班次");
		}
		binding.setStatus(blankToDefault(binding.getStatus(), "ACTIVE"));
		enterpriseMapper.upsertDepartmentShiftBinding(binding);
		auditLogService.record("考勤规则", "绑定部门班次", "department_shift_binding", binding.getId(), String.valueOf(binding.getDeptId()));
		return enterpriseMapper.findDepartmentShiftBinding(binding.getDeptId());
	}

	@Override
	public List<EmployeeShiftBinding> listEmployeeShiftBindings() {
		return enterpriseMapper.listEmployeeShiftBindings();
	}

	@Override
	public EmployeeShiftBinding saveEmployeeShiftBinding(EmployeeShiftBinding binding) {
		if (binding.getEmpId() == null || binding.getShiftId() == null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请选择员工和班次");
		}
		currentUserService.requireAccessibleEmployee(binding.getEmpId());
		binding.setStatus(blankToDefault(binding.getStatus(), "ACTIVE"));
		enterpriseMapper.upsertEmployeeShiftBinding(binding);
		auditLogService.record("考勤规则", "绑定员工班次", "employee_shift_binding", binding.getId(), String.valueOf(binding.getEmpId()));
		return enterpriseMapper.findEmployeeShiftBinding(binding.getEmpId());
	}

	@Override
	public List<AttendanceMonthLock> listAttendanceMonthLocks() {
		return enterpriseMapper.listAttendanceMonthLocks();
	}

	@Override
	public AttendanceMonthLock lockAttendanceMonth(AttendanceMonthLock lock) {
		parseMonth(lock.getCycleMonth());
		lock.setStatus("LOCKED");
		lock.setLockedBy(currentUserService.requireCurrentUser().getId());
		enterpriseMapper.upsertAttendanceMonthLock(lock);
		auditLogService.record("考勤", "锁定月份", "attendance_month_lock", lock.getId(), lock.getCycleMonth());
		return enterpriseMapper.findAttendanceMonthLock(lock.getCycleMonth());
	}

	@Override
	public AttendanceMonthLock unlockAttendanceMonth(String cycleMonth) {
		parseMonth(cycleMonth);
		AttendanceMonthLock lock = enterpriseMapper.findAttendanceMonthLock(cycleMonth);
		if (lock == null) {
			lock = new AttendanceMonthLock();
			lock.setCycleMonth(cycleMonth);
		}
		lock.setStatus("OPEN");
		lock.setLockedBy(currentUserService.requireCurrentUser().getId());
		enterpriseMapper.upsertAttendanceMonthLock(lock);
		auditLogService.record("考勤", "解锁月份", "attendance_month_lock", lock.getId(), cycleMonth);
		return enterpriseMapper.findAttendanceMonthLock(cycleMonth);
	}

	@Override
	public List<Holiday> listHolidays() {
		return enterpriseMapper.listHolidays();
	}

	@Override
	public Holiday saveHoliday(HolidayRequest request) {
		Holiday holiday = new Holiday();
		holiday.setHolidayDate(request.getHolidayDate());
		holiday.setHolidayName(request.getHolidayName());
		holiday.setHolidayType(request.getHolidayType());
		enterpriseMapper.upsertHoliday(holiday);
		auditLogService.record("考勤规则", "保存节假日", "holiday_calendar", holiday.getId(), holiday.getHolidayName());
		return holiday;
	}

	@Override
	public List<AttendanceCorrection> listCorrections() {
		List<AttendanceCorrection> rows = new ArrayList<>();
		for (AttendanceCorrection correction : enterpriseMapper.listCorrections()) {
			if (currentUserService.canAccessEmployeeRecord(correction.getEmpId())) {
				rows.add(correction);
			}
		}
		return rows;
	}

	@Override
	public AttendanceCorrection submitCorrection(AttendanceCorrectionRequest request) {
		requireEmployee(request.getEmpId());
		AttendanceCorrection correction = new AttendanceCorrection();
		correction.setEmpId(request.getEmpId());
		correction.setWorkDate(request.getWorkDate());
		correction.setCheckIn(request.getCheckIn());
		correction.setCheckOut(request.getCheckOut());
		correction.setReason(request.getReason());
		correction.setStatus("PENDING");
		correction.setAssigneeUserId(currentUserService.assigneeForEmployee(request.getEmpId()));
		enterpriseMapper.insertCorrection(correction);
		notifyEmployee(request.getEmpId(), "补卡审批待办", request.getWorkDate() + " 补卡已提交");
		auditLogService.record("考勤", "提交补卡", "attendance_correction", correction.getId(), request.getWorkDate().toString());
		return correction;
	}

	@Override
	@Transactional
	public void approveCorrection(Long id, Long approverId, String status) {
		// 补卡审批通过后写回考勤记录；月份锁定时禁止修改历史数据。
		AttendanceCorrection correction = enterpriseMapper.findCorrection(id);
		if (correction == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "补卡申请不存在");
		}
		String decision = normalizeDecision(status);
		if (isFinalStatus(correction.getStatus())) {
			return;
		}
		correction.setStatus(decision);
		correction.setApproverId(approverId);
		enterpriseMapper.updateCorrectionStatus(correction);
		if ("APPROVED".equals(decision)) {
			requireMonthUnlocked(YearMonth.from(correction.getWorkDate()).toString());
			applyAttendanceCorrection(correction);
		}
		notifyEmployee(correction.getEmpId(), "补卡审批结果", correction.getWorkDate() + "：" + correction.getStatus());
		auditLogService.record("考勤", "审批补卡", "attendance_correction", correction.getId(), correction.getStatus());
	}

	@Override
	public List<OvertimeRequest> listOvertimeRequests() {
		List<OvertimeRequest> rows = new ArrayList<>();
		for (OvertimeRequest request : enterpriseMapper.listOvertimeRequests()) {
			if (currentUserService.canAccessEmployeeRecord(request.getEmpId())) {
				rows.add(request);
			}
		}
		return rows;
	}

	@Override
	public OvertimeRequest submitOvertime(OvertimeRequestDto request) {
		requireEmployee(request.getEmpId());
		if (request.getHours() != null && request.getHours().compareTo(BigDecimal.ZERO) < 0) {
			throw new BizException(ErrorCode.BAD_REQUEST, "加班小时不能为负数");
		}
		OvertimeRequest overtime = new OvertimeRequest();
		overtime.setEmpId(request.getEmpId());
		overtime.setWorkDate(request.getWorkDate());
		overtime.setHours(nz(request.getHours()));
		overtime.setReason(request.getReason());
		overtime.setStatus("PENDING");
		overtime.setAssigneeUserId(currentUserService.assigneeForEmployee(request.getEmpId()));
		enterpriseMapper.insertOvertimeRequest(overtime);
		notifyEmployee(request.getEmpId(), "加班审批待办", request.getWorkDate() + " 加班 " + overtime.getHours() + " 小时");
		auditLogService.record("考勤", "提交加班", "overtime_request", overtime.getId(), overtime.getHours().toPlainString());
		return overtime;
	}

	@Override
	@Transactional
	public void approveOvertime(Long id, Long approverId, String status) {
		// 加班审批通过后重新生成对应月份汇总，保证薪酬读取最新考勤结果。
		OvertimeRequest overtime = enterpriseMapper.findOvertimeRequest(id);
		if (overtime == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "加班申请不存在");
		}
		String decision = normalizeDecision(status);
		if (isFinalStatus(overtime.getStatus())) {
			return;
		}
		overtime.setStatus(decision);
		overtime.setApproverId(approverId);
		enterpriseMapper.updateOvertimeStatus(overtime);
		if ("APPROVED".equals(decision) && overtime.getWorkDate() != null) {
			AttendanceSummaryRequest summaryRequest = new AttendanceSummaryRequest();
			summaryRequest.setEmpId(overtime.getEmpId());
			summaryRequest.setCycleMonth(YearMonth.from(overtime.getWorkDate()).toString());
			generateMonthlySummary(summaryRequest);
		}
		notifyEmployee(overtime.getEmpId(), "加班审批结果", overtime.getWorkDate() + "：" + overtime.getStatus());
		auditLogService.record("考勤", "审批加班", "overtime_request", overtime.getId(), overtime.getStatus());
	}

	@Override
	public List<AttendanceMonthlySummary> listMonthlySummaries() {
		List<AttendanceMonthlySummary> rows = new ArrayList<>();
		for (AttendanceMonthlySummary summary : enterpriseMapper.listMonthlySummaries()) {
			if (currentUserService.canAccessEmployeeRecord(summary.getEmpId())) {
				rows.add(summary);
			}
		}
		return rows;
	}

	@Override
	public List<AttendanceMonthlySummary> generateMonthlySummary(AttendanceSummaryRequest request) {
		YearMonth month = parseMonth(request.getCycleMonth());
		requireMonthUnlocked(month.toString());
		List<Employee> employees = employeeMapper.findAll();
		List<AttendanceMonthlySummary> result = new ArrayList<>();
		for (Employee employee : employees) {
			if (request.getEmpId() != null && !request.getEmpId().equals(employee.getId())) {
				continue;
			}
			if (!currentUserService.canAccessEmployeeRecord(employee.getId())) {
				continue;
			}
			AttendanceMonthlySummary summary = buildMonthlySummary(employee, month);
			enterpriseMapper.upsertMonthlySummary(summary);
			result.add(summary);
		}
		auditLogService.record("考勤", "生成月度汇总", "attendance_monthly_summary", null, request.getCycleMonth());
		return result;
	}

	@Override
	public List<LeaveBalance> listLeaveBalances() {
		List<LeaveBalance> rows = new ArrayList<>();
		for (LeaveBalance balance : enterpriseMapper.listLeaveBalances()) {
			if (currentUserService.canAccessEmployeeRecord(balance.getEmpId())) {
				rows.add(balance);
			}
		}
		return rows;
	}

	@Override
	public LeaveBalance saveLeaveBalance(LeaveBalanceRequest request) {
		requireEmployee(request.getEmpId());
		LeaveBalance existing = enterpriseMapper.findLeaveBalance(request.getEmpId(), request.getLeaveType(), request.getCycleYear());
		BigDecimal used = existing == null ? ZERO : nz(existing.getUsedDays());
		LeaveBalance balance = new LeaveBalance();
		balance.setEmpId(request.getEmpId());
		balance.setLeaveType(request.getLeaveType());
		balance.setTotalDays(nz(request.getTotalDays()));
		balance.setUsedDays(used);
		balance.setRemainingDays(nz(request.getTotalDays()).subtract(used).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
		balance.setCycleYear(request.getCycleYear());
		enterpriseMapper.upsertLeaveBalance(balance);
		auditLogService.record("请假", "维护假期额度", "leave_balance", balance.getId(), request.getLeaveType());
		return balance;
	}

	@Override
	@Transactional
	public void applyLeaveBalance(Long leaveRequestId) {
		// 通过业务引用检查扣减日志，保证同一请假单只扣减一次余额。
		LeaveRequest leave = leaveRequestMapper.findById(leaveRequestId);
		if (leave == null || !"APPROVED".equals(leave.getStatus())) {
			return;
		}
		if (enterpriseMapper.countLeaveBalanceLogByRef("leave_request", leave.getId()) > 0) {
			return;
		}
		int year = leave.getStartTime() == null ? LocalDate.now().getYear() : leave.getStartTime().getYear();
		LeaveBalance balance = enterpriseMapper.findLeaveBalance(leave.getEmpId(), leave.getLeaveType(), year);
		if (balance == null) {
			return;
		}
		BigDecimal days = BigDecimal.valueOf(leave.getDays()).setScale(2, RoundingMode.HALF_UP);
		if (nz(balance.getRemainingDays()).compareTo(days) < 0) {
			throw new BizException(ErrorCode.BAD_REQUEST, "假期余额不足");
		}
		String before = "used=" + balance.getUsedDays() + ", remaining=" + balance.getRemainingDays();
		balance.setUsedDays(nz(balance.getUsedDays()).add(days).setScale(2, RoundingMode.HALF_UP));
		balance.setRemainingDays(nz(balance.getRemainingDays()).subtract(days).setScale(2, RoundingMode.HALF_UP));
		enterpriseMapper.updateLeaveBalanceUsage(balance);
		enterpriseMapper.insertLeaveBalanceLog(leave.getEmpId(), leave.getLeaveType(), days.negate(), "请假审批扣减", "leave_request", leave.getId());
		String after = "used=" + balance.getUsedDays() + ", remaining=" + balance.getRemainingDays();
		auditLogService.recordChange("请假", "扣减假期余额", "leave_balance", balance.getId(), before, after, "请假ID " + leave.getId());
	}

	@Override
	public List<PerformanceGoal> listPerformanceGoals() {
		List<PerformanceGoal> rows = new ArrayList<>();
		for (PerformanceGoal goal : enterpriseMapper.listPerformanceGoals()) {
			if (currentUserService.canAccessEmployeeRecord(goal.getEmpId())) {
				rows.add(goal);
			}
		}
		return rows;
	}

	@Override
	public PerformanceGoal createPerformanceGoal(PerformanceGoalRequest request) {
		requireEmployee(request.getEmpId());
		PerformanceGoal goal = new PerformanceGoal();
		goal.setEmpId(request.getEmpId());
		goal.setCycleId(request.getCycleId());
		goal.setGoalTitle(request.getGoalTitle());
		goal.setTargetValue(request.getTargetValue());
		goal.setReviewerId(request.getReviewerId());
		goal.setStatus("GOAL_SET");
		enterpriseMapper.insertPerformanceGoal(goal);
		auditLogService.record("绩效", "制定目标", "performance_goal", goal.getId(), goal.getGoalTitle());
		return goal;
	}

	@Override
	public PerformanceGoal selfReview(Long id, PerformanceGoalScoreRequest request) {
		PerformanceGoal goal = requireGoal(id);
		goal.setSelfScore(requireScore(request.getScore()));
		goal.setStatus("SELF_REVIEWED");
		enterpriseMapper.updateSelfScore(goal);
		auditLogService.record("绩效", "员工自评", "performance_goal", goal.getId(), String.valueOf(goal.getSelfScore()));
		return enterpriseMapper.findPerformanceGoal(id);
	}

	@Override
	public PerformanceGoal managerReview(Long id, PerformanceGoalScoreRequest request) {
		PerformanceGoal goal = requireGoal(id);
		goal.setManagerScore(requireScore(request.getScore()));
		goal.setReviewerId(request.getReviewerId());
		goal.setStatus("MANAGER_REVIEWED");
		enterpriseMapper.updateManagerScore(goal);
		auditLogService.record("绩效", "上级评审", "performance_goal", goal.getId(), String.valueOf(goal.getManagerScore()));
		return enterpriseMapper.findPerformanceGoal(id);
	}

	@Override
	public PerformanceGoal calibrate(Long id, PerformanceGoalScoreRequest request) {
		PerformanceGoal goal = requireGoal(id);
		int finalScore = requireScore(request.getScore());
		goal.setFinalScore(finalScore);
		goal.setFinalLevel(blankToDefault(request.getFinalLevel(), scoreLevel(finalScore)));
		goal.setStatus("FINALIZED");
		enterpriseMapper.calibrateGoal(goal);
		auditLogService.record("绩效", "绩效校准", "performance_goal", goal.getId(), goal.getFinalLevel());
		return enterpriseMapper.findPerformanceGoal(id);
	}

	@Override
	public List<Payslip> listPayslips() {
		List<Payslip> rows = new ArrayList<>();
		for (Payslip payslip : enterpriseMapper.listPayslips()) {
			if (currentUserService.canAccessEmployeeRecord(payslip.getEmpId())) {
				rows.add(payslip);
			}
		}
		return rows;
	}

	@Override
	@Transactional
	public List<Payslip> generatePayslips(PayslipGenerateRequest request) {
		// 工资条按员工和月份唯一生成，并同步重建可追溯的工资条明细。
		YearMonth month = parseMonth(request.getCycleMonth());
		requireMonthUnlocked(month.toString());
		List<Employee> employees = employeeMapper.findAll();
		List<Payslip> result = new ArrayList<>();
		for (Employee employee : employees) {
			if (request.getEmpId() != null && !request.getEmpId().equals(employee.getId())) {
				continue;
			}
			if (!currentUserService.canAccessEmployeeRecord(employee.getId())) {
				continue;
			}
			Payslip existing = enterpriseMapper.findPayslipByEmpAndMonth(employee.getId(), month.toString());
			if (existing != null && isLockedPayslip(existing.getStatus())) {
				result.add(existing);
				continue;
			}
			Payslip payslip = buildPayslip(employee, month);
			payslip.setAssigneeUserId(currentUserService.assigneeForManagerWorkflow());
			enterpriseMapper.upsertPayslip(payslip);
			Payslip saved = enterpriseMapper.findPayslipByEmpAndMonth(employee.getId(), month.toString());
			if (saved != null) {
				replacePayslipComponents(saved);
				notifyEmployee(saved.getEmpId(), "工资条生成通知", saved.getCycleMonth() + " 工资条已生成", "PAYSLIP", saved.getId());
			}
			result.add(saved == null ? payslip : saved);
		}
		auditLogService.record("薪酬", "生成工资条", "payslip", null, request.getCycleMonth());
		return result;
	}

	@Override
	@Transactional
	public void approvePayslip(Long id, Long approverId, String status) {
		// 工资条进入最终状态后保持锁定，重复审批不再改变金额和状态。
		Payslip payslip = enterpriseMapper.findPayslip(id);
		if (payslip == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "工资条不存在");
		}
		String decision = normalizeDecision(status);
		if (isLockedPayslip(payslip.getStatus())) {
			return;
		}
		String before = payslip.getStatus();
		payslip.setStatus(decision);
		payslip.setApproverId(approverId);
		enterpriseMapper.updatePayslipStatus(payslip);
		notifyEmployee(payslip.getEmpId(), "薪酬审批结果", payslip.getCycleMonth() + " 工资条：" + payslip.getStatus());
		auditLogService.recordChange("薪酬", "审批工资条", "payslip", payslip.getId(), before, payslip.getStatus(), payslip.getCycleMonth());
	}

	@Override
	public List<PayslipComponent> listPayslipComponents(Long payslipId) {
		Payslip payslip = enterpriseMapper.findPayslip(payslipId);
		if (payslip == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "工资条不存在");
		}
		if (!currentUserService.canAccessEmployeeRecord(payslip.getEmpId())) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权访问该工资条");
		}
		return enterpriseMapper.listPayslipComponents(payslipId);
	}

	@Override
	public Payslip acknowledgePayslip(Long id) {
		Payslip payslip = enterpriseMapper.findPayslip(id);
		if (payslip == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "工资条不存在");
		}
		if (!currentUserService.canAccessEmployee(payslip.getEmpId())) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权签收该工资条");
		}
		if (!"APPROVED".equalsIgnoreCase(payslip.getStatus()) && !"PAID".equalsIgnoreCase(payslip.getStatus())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "只有已审批或已发放工资条可以签收");
		}
		payslip.setStatus("ACKNOWLEDGED");
		enterpriseMapper.acknowledgePayslip(payslip);
		notifyEmployee(payslip.getEmpId(), "工资条签收", payslip.getCycleMonth() + " 工资条已签收", "PAYSLIP_ACK", payslip.getId());
		return enterpriseMapper.findPayslip(id);
	}

	@Override
	public String exportPayslip(Long id) {
		Payslip payslip = enterpriseMapper.findPayslip(id);
		if (payslip == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "工资条不存在");
		}
		if (!currentUserService.canAccessEmployeeRecord(payslip.getEmpId())) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权导出该工资条");
		}
		StringBuilder csv = new StringBuilder("项目,金额,来源,备注\n");
		for (PayslipComponent component : enterpriseMapper.listPayslipComponents(id)) {
			csv.append(row(component.getComponentType(), component.getAmount(), component.getSourceType(), component.getRemark()));
		}
		csv.append(row("实发工资", payslip.getNetSalary(), "PAYSLIP", payslip.getCycleMonth()));
		return csv.toString();
	}

	@Override
	public List<SalaryChange> listSalaryChanges() {
		List<SalaryChange> rows = new ArrayList<>();
		for (SalaryChange change : enterpriseMapper.listSalaryChanges()) {
			if (currentUserService.canAccessEmployeeRecord(change.getEmpId())) {
				rows.add(change);
			}
		}
		return rows;
	}

	@Override
	public SalaryChange submitSalaryChange(SalaryChangeRequest request) {
		Employee employee = requireEmployee(request.getEmpId());
		SalaryChange change = new SalaryChange();
		change.setEmpId(employee.getId());
		change.setBeforeSalary(nz(employee.getBaseSalary()));
		change.setAfterSalary(nz(request.getAfterSalary()));
		change.setEffectiveMonth(request.getEffectiveMonth());
		change.setReason(request.getReason());
		change.setStatus("PENDING");
		change.setAssigneeUserId(currentUserService.assigneeForManagerWorkflow());
		enterpriseMapper.insertSalaryChange(change);
		notifyEmployee(employee.getId(), "调薪审批待办", request.getEffectiveMonth() + " 调薪已提交");
		auditLogService.record("薪酬", "提交调薪", "salary_change", change.getId(), employee.getName());
		return change;
	}

	@Override
	@Transactional
	public void approveSalaryChange(Long id, Long approverId, String status) {
		SalaryChange change = enterpriseMapper.findSalaryChange(id);
		if (change == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "调薪记录不存在");
		}
		String decision = normalizeDecision(status);
		if (isFinalStatus(change.getStatus())) {
			return;
		}
		String before = change.getStatus() + " / " + change.getBeforeSalary();
		change.setStatus(decision);
		change.setApproverId(approverId);
		enterpriseMapper.updateSalaryChangeStatus(change);
		if ("APPROVED".equals(change.getStatus())) {
			Employee employee = requireEmployee(change.getEmpId());
			employee.setBaseSalary(change.getAfterSalary());
			employeeMapper.update(employee);
			enterpriseMapper.updateSalaryStructureBase(change.getEmpId(), change.getAfterSalary());
		}
		notifyEmployee(change.getEmpId(), "调薪审批结果", change.getEffectiveMonth() + "：" + change.getStatus());
		auditLogService.recordChange("薪酬", "审批调薪", "salary_change", change.getId(),
				before, change.getStatus() + " / " + change.getAfterSalary(), change.getEffectiveMonth());
	}

	@Override
	public List<NotificationItem> listNotifications() {
		if (currentUserService.canManageAllPeople() || currentUserService.hasPermission("approval:manage")) {
			return enterpriseMapper.listNotifications();
		}
		return enterpriseMapper.listNotificationsForUser(currentUserService.currentEmpIdOrNull(),
				currentUserService.requireCurrentUser().getUsername());
	}

	@Override
	public void markNotificationRead(Long id) {
		enterpriseMapper.markNotificationRead(id);
	}

	@Override
	public List<Map<String, Object>> enterpriseOverview() {
		return enterpriseMapper.enterpriseOverview();
	}

	@Override
	public Map<String, Object> dashboardReport() {
		Map<String, Object> report = new LinkedHashMap<>();
		report.put("overview", enterpriseMapper.enterpriseOverview());
		report.put("attendanceTrend", attendanceTrend());
		report.put("leaveTrend", leaveTrend());
		report.put("departmentHeadcount", departmentHeadcount());
		report.put("performanceDistribution", performanceDistribution());
		report.put("payrollCost", payrollCost());
		report.put("approvalEfficiency", approvalEfficiency());
		return report;
	}

	@Override
	public String exportCsv(String type) {
		String normalized = type == null ? "" : type.toLowerCase();
		if ("employees".equals(normalized)) {
			return exportEmployees();
		}
		if ("attendance".equals(normalized)) {
			return exportAttendanceSummary();
		}
		if ("payroll".equals(normalized)) {
			return exportPayslips();
		}
		if ("performance".equals(normalized)) {
			return exportPerformanceGoals();
		}
		if ("reports".equals(normalized)) {
			return exportOverview();
		}
		throw new BizException(ErrorCode.BAD_REQUEST, "不支持的导出类型");
	}

	@Override
	public Map<String, Object> importEmployeesCsv(String csv) {
		Map<String, Object> result = new LinkedHashMap<>();
		List<String> errors = new ArrayList<>();
		int success = 0;
		String[] lines = csv == null ? new String[0] : csv.split("\\R");
		for (int i = 1; i < lines.length; i++) {
			if (lines[i].isBlank()) {
				continue;
			}
			String[] parts = lines[i].split(",", -1);
			try {
				if (parts.length < 5) {
					throw new IllegalArgumentException("字段不足");
				}
				Employee employee = new Employee();
				employee.setName(unquote(parts[0]));
				employee.setGender(unquote(parts[1]));
				employee.setPhone(unquote(parts[2]));
				employee.setEmail(unquote(parts[3]));
				employee.setDeptId(Long.valueOf(unquote(parts[4])));
				employee.setPosition(parts.length > 5 ? unquote(parts[5]) : null);
				employee.setStatus("ACTIVE");
				employee.setBaseSalary(parts.length > 6 && !unquote(parts[6]).isBlank() ? new BigDecimal(unquote(parts[6])) : BigDecimal.valueOf(3500));
				employeeMapper.insert(employee);
				success++;
			} catch (Exception ex) {
				errors.add("第" + (i + 1) + "行：" + ex.getMessage());
			}
		}
		result.put("successCount", success);
		result.put("failureCount", errors.size());
		result.put("errors", errors);
		return result;
	}

	@Override
	public Map<String, Object> importAttendanceCsv(String csv) {
		Map<String, Object> result = new LinkedHashMap<>();
		List<String> errors = new ArrayList<>();
		int success = 0;
		String[] lines = csv == null ? new String[0] : csv.split("\\R");
		for (int i = 1; i < lines.length; i++) {
			if (lines[i].isBlank()) {
				continue;
			}
			String[] parts = lines[i].split(",", -1);
			try {
				if (parts.length < 3) {
					throw new IllegalArgumentException("字段不足");
				}
				AttendanceRecord record = new AttendanceRecord();
				record.setEmpId(Long.valueOf(unquote(parts[0])));
				record.setWorkDate(LocalDate.parse(unquote(parts[1])));
				requireMonthUnlocked(YearMonth.from(record.getWorkDate()).toString());
				record.setStatus(unquote(parts[2]));
				if (parts.length > 3 && !unquote(parts[3]).isBlank()) {
					record.setCheckIn(java.time.LocalDateTime.parse(unquote(parts[3])));
				}
				if (parts.length > 4 && !unquote(parts[4]).isBlank()) {
					record.setCheckOut(java.time.LocalDateTime.parse(unquote(parts[4])));
				}
				attendanceRecordMapper.insert(record);
				success++;
			} catch (Exception ex) {
				errors.add("第" + (i + 1) + "行：" + ex.getMessage());
			}
		}
		result.put("successCount", success);
		result.put("failureCount", errors.size());
		result.put("errors", errors);
		return result;
	}

	private void applyLifecycle(Employee employee, LifecycleEvent event) {
		if ("REGULARIZATION".equals(event.getEventType())) {
			employee.setStatus("ACTIVE");
		} else if ("TRANSFER".equals(event.getEventType())) {
			if (event.getToDeptId() != null) {
				employee.setDeptId(event.getToDeptId());
			}
			if (event.getToPosition() != null && !event.getToPosition().isBlank()) {
				employee.setPosition(event.getToPosition());
			}
			employee.setStatus("ACTIVE");
			if (event.getTargetSalary() != null) {
				applyTransferSalaryChange(employee, event);
			}
		} else if ("RESIGNATION".equals(event.getEventType())) {
			employee.setStatus("RESIGNED");
			employee.setResignedAt(event.getEffectiveDate() == null ? LocalDate.now() : event.getEffectiveDate());
			employee.setAccountLocked(true);
		}
		employeeMapper.update(employee);
	}

	private Long assigneeForLifecycle(Employee employee, String type) {
		if ("REGULARIZATION".equals(type)) {
			return currentUserService.assigneeForEmployee(employee.getId());
		}
		return currentUserService.assigneeForManagerWorkflow();
	}

	private void applyTransferSalaryChange(Employee employee, LifecycleEvent event) {
		SalaryChange change = new SalaryChange();
		change.setEmpId(employee.getId());
		change.setBeforeSalary(nz(employee.getBaseSalary()));
		change.setAfterSalary(nz(event.getTargetSalary()));
		change.setEffectiveMonth(YearMonth.from(event.getEffectiveDate() == null ? LocalDate.now() : event.getEffectiveDate()).toString());
		change.setReason("调岗同步调薪");
		change.setStatus("APPROVED");
		change.setApproverId(event.getApproverId());
		change.setAssigneeUserId(event.getAssigneeUserId());
		enterpriseMapper.insertSalaryChange(change);
		employee.setBaseSalary(change.getAfterSalary());
		enterpriseMapper.updateSalaryStructureBase(employee.getId(), change.getAfterSalary());
	}

	private List<Map<String, Object>> attendanceTrend() {
		Map<String, Integer> buckets = new LinkedHashMap<>();
		for (AttendanceRecord record : attendanceRecordMapper.findAll()) {
			if (record.getWorkDate() == null || !currentUserService.canAccessEmployeeRecord(record.getEmpId())) {
				continue;
			}
			String key = record.getWorkDate().toString();
			String status = record.getStatus() == null ? "" : record.getStatus().toUpperCase();
			if (status.contains("LATE") || status.contains("EARLY") || status.contains("ABSENT") || "迟到".equals(record.getStatus())
					|| "早退".equals(record.getStatus()) || "缺勤".equals(record.getStatus())) {
				buckets.put(key, buckets.getOrDefault(key, 0) + 1);
			}
		}
		return mapEntries(buckets, "date", "count");
	}

	private List<Map<String, Object>> leaveTrend() {
		Map<String, Integer> buckets = new LinkedHashMap<>();
		for (LeaveRequest leave : leaveRequestMapper.findAll()) {
			if (leave.getStartTime() == null || !currentUserService.canAccessEmployeeRecord(leave.getEmpId())) {
				continue;
			}
			String key = YearMonth.from(leave.getStartTime()).toString();
			buckets.put(key, buckets.getOrDefault(key, 0) + 1);
		}
		return mapEntries(buckets, "month", "count");
	}

	private List<Map<String, Object>> departmentHeadcount() {
		Map<String, Integer> buckets = new LinkedHashMap<>();
		for (Employee employee : employeeMapper.findAll()) {
			if (!currentUserService.canAccessEmployeeRecord(employee.getId())) {
				continue;
			}
			String key = employee.getDeptId() == null ? "未分配" : String.valueOf(employee.getDeptId());
			buckets.put(key, buckets.getOrDefault(key, 0) + 1);
		}
		return mapEntries(buckets, "department", "count");
	}

	private List<Map<String, Object>> performanceDistribution() {
		Map<String, Integer> buckets = new LinkedHashMap<>();
		for (PerformanceGoal goal : enterpriseMapper.listPerformanceGoals()) {
			if (!currentUserService.canAccessEmployeeRecord(goal.getEmpId())) {
				continue;
			}
			String key = blankToDefault(goal.getFinalLevel(), "未定级");
			buckets.put(key, buckets.getOrDefault(key, 0) + 1);
		}
		return mapEntries(buckets, "level", "count");
	}

	private List<Map<String, Object>> payrollCost() {
		Map<String, BigDecimal> buckets = new LinkedHashMap<>();
		for (Payslip payslip : enterpriseMapper.listPayslips()) {
			if (!currentUserService.canAccessEmployeeRecord(payslip.getEmpId())) {
				continue;
			}
			buckets.put(payslip.getCycleMonth(), buckets.getOrDefault(payslip.getCycleMonth(), ZERO).add(nz(payslip.getNetSalary())));
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry : buckets.entrySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("month", entry.getKey());
			row.put("amount", entry.getValue());
			rows.add(row);
		}
		return rows;
	}

	private List<Map<String, Object>> approvalEfficiency() {
		Map<String, Integer> buckets = new LinkedHashMap<>();
		buckets.put("请假待审", 0);
		buckets.put("补卡待审", 0);
		buckets.put("加班待审", 0);
		buckets.put("工资待审", 0);
		for (LeaveRequest leave : leaveRequestMapper.findAll()) {
			if (isPendingStatus(leave.getStatus()) && currentUserService.canAccessEmployeeRecord(leave.getEmpId())) {
				buckets.put("请假待审", buckets.get("请假待审") + 1);
			}
		}
		for (AttendanceCorrection correction : enterpriseMapper.listCorrections()) {
			if (isPendingStatus(correction.getStatus()) && currentUserService.canAccessEmployeeRecord(correction.getEmpId())) {
				buckets.put("补卡待审", buckets.get("补卡待审") + 1);
			}
		}
		for (OvertimeRequest overtime : enterpriseMapper.listOvertimeRequests()) {
			if (isPendingStatus(overtime.getStatus()) && currentUserService.canAccessEmployeeRecord(overtime.getEmpId())) {
				buckets.put("加班待审", buckets.get("加班待审") + 1);
			}
		}
		for (Payslip payslip : enterpriseMapper.listPayslips()) {
			if (isPendingStatus(payslip.getStatus()) && currentUserService.canAccessEmployeeRecord(payslip.getEmpId())) {
				buckets.put("工资待审", buckets.get("工资待审") + 1);
			}
		}
		return mapEntries(buckets, "type", "count");
	}

	private List<Map<String, Object>> mapEntries(Map<String, Integer> buckets, String keyName, String valueName) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : buckets.entrySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put(keyName, entry.getKey());
			row.put(valueName, entry.getValue());
			rows.add(row);
		}
		return rows;
	}

	private boolean isPendingStatus(String status) {
		return "PENDING".equalsIgnoreCase(status) || "PENDING_L1".equalsIgnoreCase(status) || "PENDING_L2".equalsIgnoreCase(status);
	}

	private void requireMonthUnlocked(String cycleMonth) {
		AttendanceMonthLock lock = enterpriseMapper.findAttendanceMonthLock(cycleMonth);
		if (lock != null && "LOCKED".equalsIgnoreCase(lock.getStatus())) {
			throw new BizException(ErrorCode.BAD_REQUEST, cycleMonth + " 考勤月份已锁定，不能继续变更");
		}
	}

	private boolean isLockedPayslip(String status) {
		return "APPROVED".equalsIgnoreCase(status)
				|| "PAID".equalsIgnoreCase(status)
				|| "ACKNOWLEDGED".equalsIgnoreCase(status);
	}

	private void replacePayslipComponents(Payslip payslip) {
		enterpriseMapper.deletePayslipComponents(payslip.getId());
		addPayslipComponent(payslip, "基本工资", payslip.getBaseSalary(), "SALARY_STRUCTURE", payslip.getEmpId(), "员工基础薪资");
		addPayslipComponent(payslip, "津贴", payslip.getAllowance(), "SALARY_STRUCTURE", payslip.getEmpId(), "薪资结构津贴");
		addPayslipComponent(payslip, "奖金", payslip.getBonus(), "PERFORMANCE", payslip.getEmpId(), "绩效与固定奖金");
		addPayslipComponent(payslip, "加班费", payslip.getOvertimePay(), "ATTENDANCE_SUMMARY", payslip.getEmpId(), "月度加班汇总");
		addPayslipComponent(payslip, "请假扣款", nz(payslip.getLeaveDeduction()).negate(), "LEAVE", payslip.getEmpId(), "月度请假扣款");
		addPayslipComponent(payslip, "社保", nz(payslip.getSocialSecurity()).negate(), "SALARY_STRUCTURE", payslip.getEmpId(), "社保扣款");
		addPayslipComponent(payslip, "公积金", nz(payslip.getHousingFund()).negate(), "SALARY_STRUCTURE", payslip.getEmpId(), "公积金扣款");
		addPayslipComponent(payslip, "个税", nz(payslip.getTax()).negate(), "TAX", payslip.getEmpId(), "个人所得税");
	}

	private void addPayslipComponent(Payslip payslip, String type, BigDecimal amount, String sourceType, Long sourceId, String remark) {
		PayslipComponent component = new PayslipComponent();
		component.setPayslipId(payslip.getId());
		component.setComponentType(type);
		component.setAmount(nz(amount));
		component.setSourceType(sourceType);
		component.setSourceId(sourceId);
		component.setRemark(remark);
		enterpriseMapper.insertPayslipComponent(component);
	}

	private void applyAttendanceCorrection(AttendanceCorrection correction) {
		AttendanceRecord record = attendanceRecordMapper.findByEmpIdAndWorkDate(correction.getEmpId(), correction.getWorkDate());
		if (record == null) {
			record = new AttendanceRecord();
			record.setEmpId(correction.getEmpId());
			record.setWorkDate(correction.getWorkDate());
			record.setCheckIn(correction.getCheckIn());
			record.setCheckOut(correction.getCheckOut());
			record.setStatus("NORMAL");
			attendanceRecordMapper.insert(record);
			return;
		}
		record.setCheckIn(correction.getCheckIn() == null ? record.getCheckIn() : correction.getCheckIn());
		record.setCheckOut(correction.getCheckOut() == null ? record.getCheckOut() : correction.getCheckOut());
		record.setStatus("NORMAL");
		attendanceRecordMapper.update(record);
	}

	private AttendanceMonthlySummary buildMonthlySummary(Employee employee, YearMonth month) {
		String cycleMonth = month.toString();
		int present = 0;
		int late = 0;
		int early = 0;
		int absent = 0;
		for (AttendanceRecord record : attendanceRecordMapper.findAll()) {
			if (!employee.getId().equals(record.getEmpId()) || record.getWorkDate() == null) {
				continue;
			}
			if (!YearMonth.from(record.getWorkDate()).equals(month)) {
				continue;
			}
			String status = record.getStatus() == null ? "" : record.getStatus().toUpperCase();
			if (status.contains("ABSENT") || "缺勤".equals(record.getStatus())) {
				absent++;
			} else {
				present++;
			}
			if (status.contains("LATE") || "迟到".equals(record.getStatus())) {
				late++;
			}
			if (status.contains("EARLY") || "早退".equals(record.getStatus())) {
				early++;
			}
		}
		BigDecimal leaveDays = ZERO;
		for (LeaveRequest leave : leaveRequestMapper.findAll()) {
			if (!employee.getId().equals(leave.getEmpId()) || !"APPROVED".equals(leave.getStatus()) || leave.getStartTime() == null) {
				continue;
			}
			if (YearMonth.from(leave.getStartTime()).equals(month)) {
				leaveDays = leaveDays.add(BigDecimal.valueOf(leave.getDays()));
			}
		}
		BigDecimal overtimeHours = ZERO;
		for (OvertimeRequest overtime : enterpriseMapper.listOvertimeRequests()) {
			if (employee.getId().equals(overtime.getEmpId()) && "APPROVED".equals(overtime.getStatus())
					&& overtime.getWorkDate() != null && YearMonth.from(overtime.getWorkDate()).equals(month)) {
				overtimeHours = overtimeHours.add(nz(overtime.getHours()));
			}
		}
		AttendanceMonthlySummary summary = new AttendanceMonthlySummary();
		summary.setEmpId(employee.getId());
		summary.setCycleMonth(cycleMonth);
		summary.setPresentDays(present);
		summary.setLateCount(late);
		summary.setEarlyLeaveCount(early);
		summary.setAbsentDays(absent);
		summary.setLeaveDays(leaveDays.setScale(2, RoundingMode.HALF_UP));
		summary.setOvertimeHours(overtimeHours.setScale(2, RoundingMode.HALF_UP));
		return summary;
	}

	private Payslip buildPayslip(Employee employee, YearMonth month) {
		SalaryStructure structure = salaryStructureMapper.findByEmpId(employee.getId());
		BigDecimal base = structure == null ? nz(employee.getBaseSalary()) : nz(structure.getBaseSalary());
		BigDecimal allowance = structure == null ? ZERO : nz(structure.getAllowance());
		BigDecimal bonus = structure == null ? ZERO : nz(structure.getBonus());
		BigDecimal socialSecurity = structure == null ? ZERO : nz(structure.getSocialSecurity());
		BigDecimal housingFund = structure == null ? ZERO : nz(structure.getHousingFund());
		BigDecimal taxRate = structure == null || structure.getTaxRate() == null ? BigDecimal.valueOf(0.10) : structure.getTaxRate();

		AttendanceMonthlySummary summary = enterpriseMapper.findMonthlySummary(employee.getId(), month.toString());
		if (summary == null) {
			summary = buildMonthlySummary(employee, month);
			enterpriseMapper.upsertMonthlySummary(summary);
		}
		BigDecimal overtimePay = nz(summary.getOvertimeHours()).multiply(BigDecimal.valueOf(50)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal dailyBase = base.divide(BigDecimal.valueOf(21.75), 2, RoundingMode.HALF_UP);
		BigDecimal leaveDeduction = nz(summary.getLeaveDays()).multiply(dailyBase).setScale(2, RoundingMode.HALF_UP);
		PerformanceGoal goal = enterpriseMapper.findLatestFinalGoal(employee.getId());
		String level = goal == null ? "UNSET" : goal.getFinalLevel();
		BigDecimal performanceBonus = performanceBonus(level);
		BigDecimal gross = base.add(allowance).add(bonus).add(performanceBonus).add(overtimePay).subtract(leaveDeduction)
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal taxable = gross.subtract(socialSecurity).subtract(housingFund).max(BigDecimal.ZERO);
		BigDecimal tax = taxable.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
		BigDecimal net = gross.subtract(socialSecurity).subtract(housingFund).subtract(tax).setScale(2, RoundingMode.HALF_UP);

		Payslip payslip = new Payslip();
		payslip.setEmpId(employee.getId());
		payslip.setCycleMonth(month.toString());
		payslip.setBaseSalary(base);
		payslip.setAllowance(allowance);
		payslip.setBonus(bonus.add(performanceBonus));
		payslip.setOvertimePay(overtimePay);
		payslip.setLeaveDeduction(leaveDeduction);
		payslip.setSocialSecurity(socialSecurity);
		payslip.setHousingFund(housingFund);
		payslip.setTax(tax);
		payslip.setGrossSalary(gross);
		payslip.setNetSalary(net);
		payslip.setPerformanceLevel(level);
		payslip.setStatus("PENDING");
		return payslip;
	}

	private Employee requireEmployee(Long empId) {
		Employee employee = employeeMapper.findById(empId);
		if (employee == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "员工不存在");
		}
		return employee;
	}

	private LifecycleEvent requireLifecycle(Long id) {
		LifecycleEvent event = enterpriseMapper.findLifecycleEvent(id);
		if (event == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "生命周期记录不存在");
		}
		return event;
	}

	private PerformanceGoal requireGoal(Long id) {
		PerformanceGoal goal = enterpriseMapper.findPerformanceGoal(id);
		if (goal == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "绩效目标不存在");
		}
		return goal;
	}

	private int requireScore(Integer score) {
		if (score == null || score < 0 || score > 100) {
			throw new BizException(ErrorCode.BAD_REQUEST, "评分必须在0-100之间");
		}
		return score;
	}

	private YearMonth parseMonth(String month) {
		try {
			return YearMonth.parse(month);
		} catch (Exception ex) {
			throw new BizException(ErrorCode.BAD_REQUEST, "月份格式应为 YYYY-MM");
		}
	}

	private String normalizeLifecycleType(String type) {
		String value = type == null ? "" : type.trim().toUpperCase();
		return switch (value) {
			case "REGULARIZATION", "TRANSFER", "RESIGNATION" -> value;
			default -> throw new BizException(ErrorCode.BAD_REQUEST, "生命周期类型只支持转正、调岗、离职");
		};
	}

	private String defaultLifecycleTitle(String type) {
		return switch (type) {
			case "REGULARIZATION" -> "员工转正";
			case "TRANSFER" -> "员工调岗";
			case "RESIGNATION" -> "员工离职";
			default -> "生命周期变更";
		};
	}

	private String pendingEmployeeStatus(String type) {
		return switch (type) {
			case "REGULARIZATION" -> "REGULARIZATION_PENDING";
			case "TRANSFER" -> "TRANSFER_PENDING";
			case "RESIGNATION" -> "RESIGNATION_PENDING";
			default -> "ACTIVE";
		};
	}

	private String normalizeDecision(String status) {
		String value = status == null ? "APPROVED" : status.trim().toUpperCase();
		if (!"APPROVED".equals(value) && !"REJECTED".equals(value)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "审批状态只支持 APPROVED 或 REJECTED");
		}
		return value;
	}

	private boolean isFinalStatus(String status) {
		return "APPROVED".equals(status) || "REJECTED".equals(status);
	}

	private String employeeState(Employee employee) {
		return "status=" + employee.getStatus() + ", deptId=" + employee.getDeptId() + ", position=" + employee.getPosition();
	}

	private String scoreLevel(int score) {
		if (score >= 90) return "A";
		if (score >= 80) return "B";
		if (score >= 70) return "C";
		if (score >= 60) return "D";
		return "E";
	}

	private BigDecimal performanceBonus(String level) {
		if ("A".equalsIgnoreCase(level)) return BigDecimal.valueOf(1200);
		if ("B".equalsIgnoreCase(level)) return BigDecimal.valueOf(800);
		if ("C".equalsIgnoreCase(level)) return BigDecimal.valueOf(300);
		return ZERO;
	}

	private BigDecimal nz(BigDecimal value) {
		return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
	}

	private String blankToDefault(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private void notifyEmployee(Long empId, String title, String content) {
		notifyEmployee(empId, title, content, null, null);
	}

	private void notifyEmployee(Long empId, String title, String content, String bizType, Long bizId) {
		if (bizType != null && bizId != null
				&& enterpriseMapper.countUnreadNotificationByBiz(empId, bizType, bizId, title) > 0) {
			return;
		}
		NotificationItem notification = new NotificationItem();
		notification.setEmpId(empId);
		notification.setTitle(title);
		notification.setContent(content);
		notification.setBizType(bizType);
		notification.setBizId(bizId);
		notification.setStatus("UNREAD");
		enterpriseMapper.insertNotification(notification);
	}

	private String exportEmployees() {
		StringBuilder csv = new StringBuilder("ID,员工编号,姓名,部门ID,岗位,状态,基本工资\n");
		for (Employee e : employeeMapper.findAll()) {
			csv.append(row(e.getId(), e.getEmpNo(), e.getName(), e.getDeptId(), e.getPosition(), e.getStatus(), e.getBaseSalary()));
		}
		return csv.toString();
	}

	private String exportAttendanceSummary() {
		StringBuilder csv = new StringBuilder("ID,员工ID,月份,出勤,迟到,早退,缺勤,请假天数,加班小时\n");
		for (AttendanceMonthlySummary s : enterpriseMapper.listMonthlySummaries()) {
			csv.append(row(s.getId(), s.getEmpId(), s.getCycleMonth(), s.getPresentDays(), s.getLateCount(), s.getEarlyLeaveCount(),
					s.getAbsentDays(), s.getLeaveDays(), s.getOvertimeHours()));
		}
		return csv.toString();
	}

	private String exportPayslips() {
		StringBuilder csv = new StringBuilder("ID,员工ID,月份,税前,实发,状态,绩效等级\n");
		for (Payslip p : enterpriseMapper.listPayslips()) {
			csv.append(row(p.getId(), p.getEmpId(), p.getCycleMonth(), p.getGrossSalary(), p.getNetSalary(), p.getStatus(), p.getPerformanceLevel()));
		}
		return csv.toString();
	}

	private String exportPerformanceGoals() {
		StringBuilder csv = new StringBuilder("ID,员工ID,周期ID,目标,自评,上级评分,最终分,等级,状态\n");
		for (PerformanceGoal g : enterpriseMapper.listPerformanceGoals()) {
			csv.append(row(g.getId(), g.getEmpId(), g.getCycleId(), g.getGoalTitle(), g.getSelfScore(), g.getManagerScore(),
					g.getFinalScore(), g.getFinalLevel(), g.getStatus()));
		}
		return csv.toString();
	}

	private String exportOverview() {
		StringBuilder csv = new StringBuilder("项目,数量\n");
		for (Map<String, Object> row : enterpriseMapper.enterpriseOverview()) {
			csv.append(row(row.get("name"), row.get("value")));
		}
		return csv.toString();
	}

	private String row(Object... values) {
		List<String> parts = new ArrayList<>();
		for (Object value : values) {
			String text = value == null ? "" : String.valueOf(value);
			parts.add("\"" + text.replace("\"", "\"\"") + "\"");
		}
		return String.join(",", parts) + "\n";
	}

	private String unquote(String value) {
		String text = value == null ? "" : value.trim();
		if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
			text = text.substring(1, text.length() - 1);
		}
		return text.replace("\"\"", "\"");
	}
}
