package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.AttendanceMonthlySummary;
import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.Employee;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.Holiday;
import com.hrm.domain.LeaveBalance;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.OvertimeRequest;
import com.hrm.domain.Payslip;
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
import com.hrm.service.EnterpriseService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {

	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

	private final EnterpriseMapper enterpriseMapper;
	private final EmployeeMapper employeeMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final SalaryStructureMapper salaryStructureMapper;
	private final AuditLogService auditLogService;

	public EnterpriseServiceImpl(EnterpriseMapper enterpriseMapper,
								 EmployeeMapper employeeMapper,
								 AttendanceRecordMapper attendanceRecordMapper,
								 LeaveRequestMapper leaveRequestMapper,
								 SalaryStructureMapper salaryStructureMapper,
								 AuditLogService auditLogService) {
		this.enterpriseMapper = enterpriseMapper;
		this.employeeMapper = employeeMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.leaveRequestMapper = leaveRequestMapper;
		this.salaryStructureMapper = salaryStructureMapper;
		this.auditLogService = auditLogService;
	}

	@Override
	public List<EmployeeContract> listContracts() {
		return enterpriseMapper.listContracts();
	}

	@Override
	public EmployeeContract createContract(ContractRequest request) {
		Employee employee = requireEmployee(request.getEmpId());
		EmployeeContract contract = new EmployeeContract();
		contract.setEmpId(request.getEmpId());
		contract.setContractNo(request.getContractNo());
		contract.setContractType(request.getContractType());
		contract.setStartDate(request.getStartDate());
		contract.setEndDate(request.getEndDate());
		contract.setStatus(blankToDefault(request.getStatus(), "ACTIVE"));
		enterpriseMapper.insertContract(contract);
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
		auditLogService.record("员工生命周期", "合同", "employee_contract", contract.getId(), employee.getName());
		return contract;
	}

	@Override
	public List<LifecycleEvent> listLifecycleEvents() {
		return enterpriseMapper.listLifecycleEvents();
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
		return enterpriseMapper.listCorrections();
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
		enterpriseMapper.insertCorrection(correction);
		notifyEmployee(request.getEmpId(), "补卡审批待办", request.getWorkDate() + " 补卡已提交");
		auditLogService.record("考勤", "提交补卡", "attendance_correction", correction.getId(), request.getWorkDate().toString());
		return correction;
	}

	@Override
	@Transactional
	public void approveCorrection(Long id, Long approverId, String status) {
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
			applyAttendanceCorrection(correction);
		}
		notifyEmployee(correction.getEmpId(), "补卡审批结果", correction.getWorkDate() + "：" + correction.getStatus());
		auditLogService.record("考勤", "审批补卡", "attendance_correction", correction.getId(), correction.getStatus());
	}

	@Override
	public List<OvertimeRequest> listOvertimeRequests() {
		return enterpriseMapper.listOvertimeRequests();
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
		enterpriseMapper.insertOvertimeRequest(overtime);
		notifyEmployee(request.getEmpId(), "加班审批待办", request.getWorkDate() + " 加班 " + overtime.getHours() + " 小时");
		auditLogService.record("考勤", "提交加班", "overtime_request", overtime.getId(), overtime.getHours().toPlainString());
		return overtime;
	}

	@Override
	@Transactional
	public void approveOvertime(Long id, Long approverId, String status) {
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
		return enterpriseMapper.listMonthlySummaries();
	}

	@Override
	public List<AttendanceMonthlySummary> generateMonthlySummary(AttendanceSummaryRequest request) {
		YearMonth month = parseMonth(request.getCycleMonth());
		List<Employee> employees = employeeMapper.findAll();
		List<AttendanceMonthlySummary> result = new ArrayList<>();
		for (Employee employee : employees) {
			if (request.getEmpId() != null && !request.getEmpId().equals(employee.getId())) {
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
		return enterpriseMapper.listLeaveBalances();
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
		return enterpriseMapper.listPerformanceGoals();
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
		return enterpriseMapper.listPayslips();
	}

	@Override
	@Transactional
	public List<Payslip> generatePayslips(PayslipGenerateRequest request) {
		YearMonth month = parseMonth(request.getCycleMonth());
		List<Employee> employees = employeeMapper.findAll();
		List<Payslip> result = new ArrayList<>();
		for (Employee employee : employees) {
			if (request.getEmpId() != null && !request.getEmpId().equals(employee.getId())) {
				continue;
			}
			Payslip existing = enterpriseMapper.findPayslipByEmpAndMonth(employee.getId(), month.toString());
			if (existing != null && "APPROVED".equals(existing.getStatus())) {
				result.add(existing);
				continue;
			}
			Payslip payslip = buildPayslip(employee, month);
			enterpriseMapper.upsertPayslip(payslip);
			result.add(payslip);
		}
		auditLogService.record("薪酬", "生成工资条", "payslip", null, request.getCycleMonth());
		return result;
	}

	@Override
	@Transactional
	public void approvePayslip(Long id, Long approverId, String status) {
		Payslip payslip = enterpriseMapper.findPayslip(id);
		if (payslip == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "工资条不存在");
		}
		String decision = normalizeDecision(status);
		if (isFinalStatus(payslip.getStatus())) {
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
	public List<SalaryChange> listSalaryChanges() {
		return enterpriseMapper.listSalaryChanges();
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
		return enterpriseMapper.listNotifications();
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
		} else if ("RESIGNATION".equals(event.getEventType())) {
			employee.setStatus("RESIGNED");
		}
		employeeMapper.update(employee);
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
		NotificationItem notification = new NotificationItem();
		notification.setEmpId(empId);
		notification.setTitle(title);
		notification.setContent(content);
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
}
