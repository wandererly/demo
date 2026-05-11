package com.hrm.service;

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
import java.util.List;
import java.util.Map;

public interface EnterpriseService {
	List<EmployeeContract> listContracts();
	EmployeeContract createContract(ContractRequest request);
	List<LifecycleEvent> listLifecycleEvents();
	LifecycleEvent submitLifecycleEvent(LifecycleEventRequest request);
	void approveLifecycle(Long id, Long approverId, String status);

	List<WorkShift> listShifts();
	WorkShift createShift(ShiftRequest request);
	List<Holiday> listHolidays();
	Holiday saveHoliday(HolidayRequest request);
	List<AttendanceCorrection> listCorrections();
	AttendanceCorrection submitCorrection(AttendanceCorrectionRequest request);
	void approveCorrection(Long id, Long approverId, String status);
	List<OvertimeRequest> listOvertimeRequests();
	OvertimeRequest submitOvertime(OvertimeRequestDto request);
	void approveOvertime(Long id, Long approverId, String status);
	List<AttendanceMonthlySummary> listMonthlySummaries();
	List<AttendanceMonthlySummary> generateMonthlySummary(AttendanceSummaryRequest request);

	List<LeaveBalance> listLeaveBalances();
	LeaveBalance saveLeaveBalance(LeaveBalanceRequest request);
	void applyLeaveBalance(Long leaveRequestId);

	List<PerformanceGoal> listPerformanceGoals();
	PerformanceGoal createPerformanceGoal(PerformanceGoalRequest request);
	PerformanceGoal selfReview(Long id, PerformanceGoalScoreRequest request);
	PerformanceGoal managerReview(Long id, PerformanceGoalScoreRequest request);
	PerformanceGoal calibrate(Long id, PerformanceGoalScoreRequest request);

	List<Payslip> listPayslips();
	List<Payslip> generatePayslips(PayslipGenerateRequest request);
	void approvePayslip(Long id, Long approverId, String status);
	List<SalaryChange> listSalaryChanges();
	SalaryChange submitSalaryChange(SalaryChangeRequest request);
	void approveSalaryChange(Long id, Long approverId, String status);

	List<NotificationItem> listNotifications();
	void markNotificationRead(Long id);
	List<Map<String, Object>> enterpriseOverview();
	String exportCsv(String type);
}
