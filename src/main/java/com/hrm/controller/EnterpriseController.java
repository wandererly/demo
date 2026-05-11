package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.AttendanceMonthlySummary;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.Holiday;
import com.hrm.domain.LeaveBalance;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.NotificationItem;
import com.hrm.domain.OvertimeRequest;
import com.hrm.domain.Payslip;
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

	@GetMapping("/export/{type}")
	@PreAuthorize("hasAuthority('report:view') or hasAuthority('hr:manage') or hasAuthority('payroll:manage')")
	public ResponseEntity<byte[]> exportCsv(@PathVariable String type) {
		byte[] body = enterpriseService.exportCsv(type).getBytes(StandardCharsets.UTF_8);
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(type + ".csv", StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(body);
	}
}
