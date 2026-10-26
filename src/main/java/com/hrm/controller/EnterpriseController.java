package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.AttendanceMonthLock;
import com.hrm.domain.AttendanceMonthlySummary;
import com.hrm.domain.AttendanceRuleConfig;
import com.hrm.domain.DepartmentShiftBinding;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.EmployeeShiftBinding;
import com.hrm.domain.Holiday;
import com.hrm.domain.LeaveBalance;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.OvertimeRequest;
import com.hrm.domain.Payslip;
import com.hrm.domain.PayslipComponent;
import com.hrm.domain.PerformanceGoal;
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
import com.hrm.service.EnterpriseService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class EnterpriseController {

	private final EnterpriseService enterpriseService;

	public EnterpriseController(EnterpriseService enterpriseService) {
		this.enterpriseService = enterpriseService;
	}

	@GetMapping("/lifecycle/contracts")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<List<EmployeeContract>> listContracts() {
		return ApiResponse.ok(enterpriseService.listContracts());
	}

	@PostMapping("/lifecycle/contracts")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<EmployeeContract> createContract(@Valid @RequestBody ContractRequest request) {
		return ApiResponse.ok(enterpriseService.createContract(request));
	}

	@GetMapping("/lifecycle/events")
	@PreAuthorize("hasAuthority('hr:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<LifecycleEvent>> listLifecycleEvents() {
		return ApiResponse.ok(enterpriseService.listLifecycleEvents());
	}

	@PostMapping("/lifecycle/events")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<LifecycleEvent> submitLifecycleEvent(@Valid @RequestBody LifecycleEventRequest request) {
		return ApiResponse.ok(enterpriseService.submitLifecycleEvent(request));
	}

	@GetMapping("/attendance-rules/shifts")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<WorkShift>> listShifts() {
		return ApiResponse.ok(enterpriseService.listShifts());
	}

	@PostMapping("/attendance-rules/shifts")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<WorkShift> createShift(@Valid @RequestBody ShiftRequest request) {
		return ApiResponse.ok(enterpriseService.createShift(request));
	}

	@GetMapping("/attendance-rules/config")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<AttendanceRuleConfig> getAttendanceRuleConfig() {
		return ApiResponse.ok(enterpriseService.getAttendanceRuleConfig());
	}

	@PostMapping("/attendance-rules/config")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<AttendanceRuleConfig> saveAttendanceRuleConfig(@RequestBody AttendanceRuleConfig config) {
		return ApiResponse.ok(enterpriseService.saveAttendanceRuleConfig(config));
	}

	@GetMapping("/attendance-rules/shift-bindings/departments")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<DepartmentShiftBinding>> listDepartmentShiftBindings() {
		return ApiResponse.ok(enterpriseService.listDepartmentShiftBindings());
	}

	@PostMapping("/attendance-rules/shift-bindings/departments")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<DepartmentShiftBinding> saveDepartmentShiftBinding(@RequestBody DepartmentShiftBinding binding) {
		return ApiResponse.ok(enterpriseService.saveDepartmentShiftBinding(binding));
	}

	@GetMapping("/attendance-rules/shift-bindings/employees")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<EmployeeShiftBinding>> listEmployeeShiftBindings() {
		return ApiResponse.ok(enterpriseService.listEmployeeShiftBindings());
	}

	@PostMapping("/attendance-rules/shift-bindings/employees")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<EmployeeShiftBinding> saveEmployeeShiftBinding(@RequestBody EmployeeShiftBinding binding) {
		return ApiResponse.ok(enterpriseService.saveEmployeeShiftBinding(binding));
	}

	@GetMapping("/attendance-rules/month-locks")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<AttendanceMonthLock>> listAttendanceMonthLocks() {
		return ApiResponse.ok(enterpriseService.listAttendanceMonthLocks());
	}

	@PostMapping("/attendance-rules/month-lock")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<AttendanceMonthLock> lockAttendanceMonth(@RequestBody AttendanceMonthLock lock) {
		return ApiResponse.ok(enterpriseService.lockAttendanceMonth(lock));
	}

	@PostMapping("/attendance-rules/month-unlock/{cycleMonth}")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<AttendanceMonthLock> unlockAttendanceMonth(@PathVariable String cycleMonth) {
		return ApiResponse.ok(enterpriseService.unlockAttendanceMonth(cycleMonth));
	}

	@GetMapping("/attendance-rules/holidays")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<Holiday>> listHolidays() {
		return ApiResponse.ok(enterpriseService.listHolidays());
	}

	@PostMapping("/attendance-rules/holidays")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<Holiday> saveHoliday(@Valid @RequestBody HolidayRequest request) {
		return ApiResponse.ok(enterpriseService.saveHoliday(request));
	}

	@GetMapping("/attendance-rules/corrections")
	@PreAuthorize("hasAuthority('attendance:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<AttendanceCorrection>> listCorrections() {
		return ApiResponse.ok(enterpriseService.listCorrections());
	}

	@PostMapping("/attendance-rules/corrections")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<AttendanceCorrection> submitCorrection(@Valid @RequestBody AttendanceCorrectionRequest request) {
		return ApiResponse.ok(enterpriseService.submitCorrection(request));
	}

	@GetMapping("/attendance-rules/overtime")
	@PreAuthorize("hasAuthority('attendance:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<OvertimeRequest>> listOvertime() {
		return ApiResponse.ok(enterpriseService.listOvertimeRequests());
	}

	@PostMapping("/attendance-rules/overtime")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<OvertimeRequest> submitOvertime(@Valid @RequestBody OvertimeRequestDto request) {
		return ApiResponse.ok(enterpriseService.submitOvertime(request));
	}

	@GetMapping("/attendance-rules/monthly-summary")
	@PreAuthorize("hasAuthority('attendance:manage') or hasAuthority('report:view')")
	public ApiResponse<List<AttendanceMonthlySummary>> listMonthlySummaries() {
		return ApiResponse.ok(enterpriseService.listMonthlySummaries());
	}

	@PostMapping("/attendance-rules/monthly-summary/generate")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<List<AttendanceMonthlySummary>> generateMonthlySummary(@Valid @RequestBody AttendanceSummaryRequest request) {
		return ApiResponse.ok(enterpriseService.generateMonthlySummary(request));
	}

	@GetMapping("/leave-balances")
	@PreAuthorize("hasAuthority('leave:manage')")
	public ApiResponse<List<LeaveBalance>> listLeaveBalances() {
		return ApiResponse.ok(enterpriseService.listLeaveBalances());
	}

	@PostMapping("/leave-balances")
	@PreAuthorize("hasAuthority('leave:manage')")
	public ApiResponse<LeaveBalance> saveLeaveBalance(@Valid @RequestBody LeaveBalanceRequest request) {
		return ApiResponse.ok(enterpriseService.saveLeaveBalance(request));
	}

	@GetMapping("/performance-goals")
	@PreAuthorize("hasAuthority('perf:manage')")
	public ApiResponse<List<PerformanceGoal>> listPerformanceGoals() {
		return ApiResponse.ok(enterpriseService.listPerformanceGoals());
	}

	@PostMapping("/performance-goals")
	@PreAuthorize("hasAuthority('perf:manage')")
	public ApiResponse<PerformanceGoal> createPerformanceGoal(@Valid @RequestBody PerformanceGoalRequest request) {
		return ApiResponse.ok(enterpriseService.createPerformanceGoal(request));
	}

	@PutMapping("/performance-goals/{id}/self-review")
	@PreAuthorize("hasAuthority('perf:manage')")
	public ApiResponse<PerformanceGoal> selfReview(@PathVariable Long id,
										 @Valid @RequestBody PerformanceGoalScoreRequest request) {
		return ApiResponse.ok(enterpriseService.selfReview(id, request));
	}

	@PutMapping("/performance-goals/{id}/manager-review")
	@PreAuthorize("hasAuthority('perf:manage')")
	public ApiResponse<PerformanceGoal> managerReview(@PathVariable Long id,
										   @Valid @RequestBody PerformanceGoalScoreRequest request) {
		return ApiResponse.ok(enterpriseService.managerReview(id, request));
	}

	@PutMapping("/performance-goals/{id}/calibrate")
	@PreAuthorize("hasAuthority('perf:manage')")
	public ApiResponse<PerformanceGoal> calibrate(@PathVariable Long id,
										@Valid @RequestBody PerformanceGoalScoreRequest request) {
		return ApiResponse.ok(enterpriseService.calibrate(id, request));
	}

	@GetMapping("/payroll/payslips")
	@PreAuthorize("hasAuthority('payroll:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<Payslip>> listPayslips() {
		return ApiResponse.ok(enterpriseService.listPayslips());
	}

	@GetMapping("/payroll/payslips/{id}/components")
	@PreAuthorize("hasAuthority('payroll:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<PayslipComponent>> listPayslipComponents(@PathVariable Long id) {
		return ApiResponse.ok(enterpriseService.listPayslipComponents(id));
	}

	@PostMapping("/payroll/payslips/generate")
	@PreAuthorize("hasAuthority('payroll:manage')")
	public ApiResponse<List<Payslip>> generatePayslips(@Valid @RequestBody PayslipGenerateRequest request) {
		return ApiResponse.ok(enterpriseService.generatePayslips(request));
	}

	@GetMapping("/payroll/salary-changes")
	@PreAuthorize("hasAuthority('payroll:manage') or hasAuthority('approval:manage')")
	public ApiResponse<List<SalaryChange>> listSalaryChanges() {
		return ApiResponse.ok(enterpriseService.listSalaryChanges());
	}

	@PostMapping("/payroll/salary-changes")
	@PreAuthorize("hasAuthority('payroll:manage')")
	public ApiResponse<SalaryChange> submitSalaryChange(@Valid @RequestBody SalaryChangeRequest request) {
		return ApiResponse.ok(enterpriseService.submitSalaryChange(request));
	}

	@GetMapping("/notifications")
	@PreAuthorize("hasAuthority('approval:manage') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ApiResponse<List<NotificationItem>> listNotifications() {
		return ApiResponse.ok(enterpriseService.listNotifications());
	}

	@PostMapping("/notifications/{id}/read")
	@PreAuthorize("hasAuthority('approval:manage') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ApiResponse<Void> markNotificationRead(@PathVariable Long id) {
		enterpriseService.markNotificationRead(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/reports/enterprise")
	@PreAuthorize("hasAuthority('report:view') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ApiResponse<List<Map<String, Object>>> enterpriseOverview() {
		return ApiResponse.ok(enterpriseService.enterpriseOverview());
	}

	@GetMapping("/reports/dashboard")
	@PreAuthorize("hasAuthority('report:view') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ApiResponse<Map<String, Object>> dashboardReport() {
		return ApiResponse.ok(enterpriseService.dashboardReport());
	}

	@PostMapping("/import/employees")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<Map<String, Object>> importEmployees(@RequestBody byte[] body) {
		return ApiResponse.ok(enterpriseService.importEmployeesCsv(new String(body, StandardCharsets.UTF_8)));
	}

	@PostMapping(value = "/import/employees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<Map<String, Object>> importEmployeesFile(@org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file) throws java.io.IOException {
		return ApiResponse.ok(enterpriseService.importEmployeesCsv(new String(file.getBytes(), StandardCharsets.UTF_8)));
	}

	@PostMapping("/import/attendance")
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<Map<String, Object>> importAttendance(@RequestBody byte[] body) {
		return ApiResponse.ok(enterpriseService.importAttendanceCsv(new String(body, StandardCharsets.UTF_8)));
	}

	@PostMapping(value = "/import/attendance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('attendance:manage')")
	public ApiResponse<Map<String, Object>> importAttendanceFile(@org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file) throws java.io.IOException {
		return ApiResponse.ok(enterpriseService.importAttendanceCsv(new String(file.getBytes(), StandardCharsets.UTF_8)));
	}

	@GetMapping("/export/employees")
	@PreAuthorize("hasAuthority('hr:manage') or hasAuthority('report:view')")
	public ResponseEntity<byte[]> exportEmployees() {
		return csv("employees", enterpriseService.exportCsv("employees"));
	}

	@GetMapping("/export/attendance")
	@PreAuthorize("hasAuthority('attendance:manage') or hasAuthority('report:view')")
	public ResponseEntity<byte[]> exportAttendance() {
		return csv("attendance", enterpriseService.exportCsv("attendance"));
	}

	@GetMapping("/export/payslips")
	@PreAuthorize("hasAuthority('payroll:manage') or hasAuthority('report:view')")
	public ResponseEntity<byte[]> exportPayslips() {
		return csv("payslips", enterpriseService.exportCsv("payroll"));
	}

	@GetMapping("/export/payslips/{id}")
	@PreAuthorize("hasAuthority('payroll:manage') or hasAuthority('report:view')")
	public ResponseEntity<byte[]> exportPayslip(@PathVariable Long id) {
		return csv("payslip-" + id, enterpriseService.exportPayslip(id));
	}

	@GetMapping("/export/reports")
	@PreAuthorize("hasAuthority('report:view') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ResponseEntity<byte[]> exportReports() {
		return csv("reports", enterpriseService.exportCsv("reports"));
	}

	@GetMapping("/export/{type}")
	@PreAuthorize("hasAuthority('report:view') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ResponseEntity<byte[]> exportCsv(@PathVariable String type) {
		return csv(type, enterpriseService.exportCsv(type));
	}

	private ResponseEntity<byte[]> csv(String type, String content) {
		byte[] body = content.getBytes(StandardCharsets.UTF_8);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(type + ".csv", StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(body);
	}
}
