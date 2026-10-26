package com.hrm.mapper;

import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.AttendanceMonthLock;
import com.hrm.domain.AttendanceMonthlySummary;
import com.hrm.domain.AttendanceRuleConfig;
import com.hrm.domain.DepartmentShiftBinding;
import com.hrm.domain.EmployeeShiftBinding;
import com.hrm.domain.EmployeeContract;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EnterpriseMapper {

	@Select("select id, emp_id, contract_no, contract_type, start_date, end_date, status, probation_start_date, probation_end_date, renewal_status, reminder_sent, created_at, updated_at "
			+ "from employee_contract order by id desc")
	List<EmployeeContract> listContracts();

	@Insert("insert into employee_contract(emp_id, contract_no, contract_type, start_date, end_date, status, probation_start_date, probation_end_date, renewal_status, reminder_sent, created_at, updated_at) "
			+ "values(#{empId}, #{contractNo}, #{contractType}, #{startDate}, #{endDate}, #{status}, #{probationStartDate}, #{probationEndDate}, #{renewalStatus}, #{reminderSent}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertContract(EmployeeContract contract);

	@Select("select id, emp_id, event_type, title, from_dept_id, to_dept_id, from_position, to_position, effective_date, "
			+ "status, approver_id, assignee_user_id, approved_at, target_salary, account_action, asset_handover_status, detail, created_at, updated_at from employee_lifecycle_event order by id desc")
	List<LifecycleEvent> listLifecycleEvents();

	@Select("select id, emp_id, event_type, title, from_dept_id, to_dept_id, from_position, to_position, effective_date, "
			+ "status, approver_id, assignee_user_id, approved_at, target_salary, account_action, asset_handover_status, detail, created_at, updated_at from employee_lifecycle_event where id = #{id}")
	LifecycleEvent findLifecycleEvent(Long id);

	@Insert("insert into employee_lifecycle_event(emp_id, event_type, title, from_dept_id, to_dept_id, from_position, to_position, "
			+ "effective_date, status, assignee_user_id, target_salary, account_action, asset_handover_status, detail, created_at, updated_at) values(#{empId}, #{eventType}, #{title}, #{fromDeptId}, "
			+ "#{toDeptId}, #{fromPosition}, #{toPosition}, #{effectiveDate}, #{status}, #{assigneeUserId}, #{targetSalary}, #{accountAction}, #{assetHandoverStatus}, #{detail}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertLifecycleEvent(LifecycleEvent event);

	@Update("update employee_lifecycle_event set status = #{status}, approver_id = #{approverId}, approved_at = now(), updated_at = now() where id = #{id}")
	int updateLifecycleStatus(LifecycleEvent event);

	@Select("select id, shift_name, start_time, end_time, work_hours, status, created_at from work_shift order by id desc")
	List<WorkShift> listShifts();

	@Insert("insert into work_shift(shift_name, start_time, end_time, work_hours, status, created_at) "
			+ "values(#{shiftName}, #{startTime}, #{endTime}, #{workHours}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertShift(WorkShift shift);

	@Select("select id, holiday_date, holiday_name, holiday_type, created_at from holiday_calendar order by holiday_date desc")
	List<Holiday> listHolidays();

	@Insert("insert into holiday_calendar(holiday_date, holiday_name, holiday_type, created_at) "
			+ "values(#{holidayDate}, #{holidayName}, #{holidayType}, now()) "
			+ "on duplicate key update holiday_name = values(holiday_name), holiday_type = values(holiday_type)")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int upsertHoliday(Holiday holiday);

	@Select("select id, emp_id, work_date, check_in, check_out, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from attendance_correction order by id desc")
	List<AttendanceCorrection> listCorrections();

	@Select("select id, emp_id, work_date, check_in, check_out, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from attendance_correction where id = #{id}")
	AttendanceCorrection findCorrection(Long id);

	@Insert("insert into attendance_correction(emp_id, work_date, check_in, check_out, reason, status, assignee_user_id, created_at, updated_at) "
			+ "values(#{empId}, #{workDate}, #{checkIn}, #{checkOut}, #{reason}, #{status}, #{assigneeUserId}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertCorrection(AttendanceCorrection correction);

	@Update("update attendance_correction set status = #{status}, approver_id = #{approverId}, approved_at = now(), updated_at = now() where id = #{id}")
	int updateCorrectionStatus(AttendanceCorrection correction);

	@Select("select id, emp_id, work_date, hours, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from overtime_request order by id desc")
	List<OvertimeRequest> listOvertimeRequests();

	@Select("select id, emp_id, work_date, hours, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from overtime_request where id = #{id}")
	OvertimeRequest findOvertimeRequest(Long id);

	@Insert("insert into overtime_request(emp_id, work_date, hours, reason, status, assignee_user_id, created_at, updated_at) "
			+ "values(#{empId}, #{workDate}, #{hours}, #{reason}, #{status}, #{assigneeUserId}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertOvertimeRequest(OvertimeRequest request);

	@Update("update overtime_request set status = #{status}, approver_id = #{approverId}, approved_at = now(), updated_at = now() where id = #{id}")
	int updateOvertimeStatus(OvertimeRequest request);

	@Select("select id, emp_id, cycle_month, present_days, late_count, early_leave_count, absent_days, leave_days, overtime_hours, "
			+ "created_at, updated_at from attendance_monthly_summary order by cycle_month desc, emp_id asc")
	List<AttendanceMonthlySummary> listMonthlySummaries();

	@Select("select id, emp_id, cycle_month, present_days, late_count, early_leave_count, absent_days, leave_days, overtime_hours, "
			+ "created_at, updated_at from attendance_monthly_summary where emp_id = #{empId} and cycle_month = #{cycleMonth}")
	AttendanceMonthlySummary findMonthlySummary(@Param("empId") Long empId, @Param("cycleMonth") String cycleMonth);

	@Insert("insert into attendance_monthly_summary(emp_id, cycle_month, present_days, late_count, early_leave_count, absent_days, "
			+ "leave_days, overtime_hours, created_at, updated_at) values(#{empId}, #{cycleMonth}, #{presentDays}, #{lateCount}, "
			+ "#{earlyLeaveCount}, #{absentDays}, #{leaveDays}, #{overtimeHours}, now(), now()) "
			+ "on duplicate key update present_days = values(present_days), late_count = values(late_count), "
			+ "early_leave_count = values(early_leave_count), absent_days = values(absent_days), leave_days = values(leave_days), "
			+ "overtime_hours = values(overtime_hours), updated_at = now()")
	int upsertMonthlySummary(AttendanceMonthlySummary summary);

	@Select("select id, cycle_month, status, locked_by, locked_at, remark from attendance_month_lock order by cycle_month desc")
	List<AttendanceMonthLock> listAttendanceMonthLocks();

	@Select("select id, cycle_month, status, locked_by, locked_at, remark from attendance_month_lock where cycle_month = #{cycleMonth}")
	AttendanceMonthLock findAttendanceMonthLock(String cycleMonth);

	@Insert("insert into attendance_month_lock(cycle_month, status, locked_by, locked_at, remark) "
			+ "values(#{cycleMonth}, #{status}, #{lockedBy}, now(), #{remark}) "
			+ "on duplicate key update status = values(status), locked_by = values(locked_by), locked_at = now(), remark = values(remark)")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int upsertAttendanceMonthLock(AttendanceMonthLock lock);

	@Select("select id, work_start_time, work_end_time, late_grace_minutes, early_leave_grace_minutes, absence_rule, "
			+ "generate_baseline_on_holiday, generate_baseline_on_workday, updated_at from attendance_rule_config order by id desc limit 1")
	AttendanceRuleConfig findAttendanceRuleConfig();

	@Insert("insert into attendance_rule_config(work_start_time, work_end_time, late_grace_minutes, early_leave_grace_minutes, absence_rule, "
			+ "generate_baseline_on_holiday, generate_baseline_on_workday, updated_at) values(#{workStartTime}, #{workEndTime}, #{lateGraceMinutes}, "
			+ "#{earlyLeaveGraceMinutes}, #{absenceRule}, #{generateBaselineOnHoliday}, #{generateBaselineOnWorkday}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertAttendanceRuleConfig(AttendanceRuleConfig config);

	@Update("update attendance_rule_config set work_start_time = #{workStartTime}, work_end_time = #{workEndTime}, late_grace_minutes = #{lateGraceMinutes}, "
			+ "early_leave_grace_minutes = #{earlyLeaveGraceMinutes}, absence_rule = #{absenceRule}, generate_baseline_on_holiday = #{generateBaselineOnHoliday}, "
			+ "generate_baseline_on_workday = #{generateBaselineOnWorkday}, updated_at = now() where id = #{id}")
	int updateAttendanceRuleConfig(AttendanceRuleConfig config);

	@Select("select id, dept_id, shift_id, status, updated_at from department_shift_binding order by id desc")
	List<DepartmentShiftBinding> listDepartmentShiftBindings();

	@Select("select id, dept_id, shift_id, status, updated_at from department_shift_binding where dept_id = #{deptId}")
	DepartmentShiftBinding findDepartmentShiftBinding(Long deptId);

	@Insert("insert into department_shift_binding(dept_id, shift_id, status, updated_at) values(#{deptId}, #{shiftId}, #{status}, now()) "
			+ "on duplicate key update shift_id = values(shift_id), status = values(status), updated_at = now()")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int upsertDepartmentShiftBinding(DepartmentShiftBinding binding);

	@Select("select id, emp_id, shift_id, status, updated_at from employee_shift_binding order by id desc")
	List<EmployeeShiftBinding> listEmployeeShiftBindings();

	@Select("select id, emp_id, shift_id, status, updated_at from employee_shift_binding where emp_id = #{empId}")
	EmployeeShiftBinding findEmployeeShiftBinding(Long empId);

	@Insert("insert into employee_shift_binding(emp_id, shift_id, status, updated_at) values(#{empId}, #{shiftId}, #{status}, now()) "
			+ "on duplicate key update shift_id = values(shift_id), status = values(status), updated_at = now()")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int upsertEmployeeShiftBinding(EmployeeShiftBinding binding);

	@Select("select id, emp_id, leave_type, total_days, used_days, remaining_days, cycle_year, updated_at from leave_balance "
			+ "order by cycle_year desc, emp_id asc, leave_type asc")
	List<LeaveBalance> listLeaveBalances();

	@Select("select id, emp_id, leave_type, total_days, used_days, remaining_days, cycle_year, updated_at from leave_balance "
			+ "where emp_id = #{empId} and leave_type = #{leaveType} and cycle_year = #{cycleYear}")
	LeaveBalance findLeaveBalance(@Param("empId") Long empId, @Param("leaveType") String leaveType, @Param("cycleYear") int cycleYear);

	@Insert("insert into leave_balance(emp_id, leave_type, total_days, used_days, remaining_days, cycle_year, updated_at) "
			+ "values(#{empId}, #{leaveType}, #{totalDays}, #{usedDays}, #{remainingDays}, #{cycleYear}, now()) "
			+ "on duplicate key update total_days = values(total_days), remaining_days = values(remaining_days), updated_at = now()")
	int upsertLeaveBalance(LeaveBalance balance);

	@Update("update leave_balance set used_days = #{usedDays}, remaining_days = #{remainingDays}, updated_at = now() where id = #{id}")
	int updateLeaveBalanceUsage(LeaveBalance balance);

	@Insert("insert into leave_balance_log(emp_id, leave_type, change_days, reason, ref_type, ref_id, created_at) "
			+ "values(#{empId}, #{leaveType}, #{changeDays}, #{reason}, #{refType}, #{refId}, now())")
	int insertLeaveBalanceLog(@Param("empId") Long empId, @Param("leaveType") String leaveType,
							  @Param("changeDays") BigDecimal changeDays, @Param("reason") String reason,
							  @Param("refType") String refType, @Param("refId") Long refId);

	@Select("select count(*) from leave_balance_log where ref_type = #{refType} and ref_id = #{refId}")
	int countLeaveBalanceLogByRef(@Param("refType") String refType, @Param("refId") Long refId);

	@Select("select id, emp_id, cycle_id, goal_title, target_value, self_score, manager_score, final_score, final_level, "
			+ "status, reviewer_id, calibrated_at, created_at, updated_at from performance_goal order by id desc")
	List<PerformanceGoal> listPerformanceGoals();

	@Select("select id, emp_id, cycle_id, goal_title, target_value, self_score, manager_score, final_score, final_level, "
			+ "status, reviewer_id, calibrated_at, created_at, updated_at from performance_goal where id = #{id}")
	PerformanceGoal findPerformanceGoal(Long id);

	@Select("select id, emp_id, cycle_id, goal_title, target_value, self_score, manager_score, final_score, final_level, "
			+ "status, reviewer_id, calibrated_at, created_at, updated_at from performance_goal "
			+ "where emp_id = #{empId} order by id desc")
	List<PerformanceGoal> listPerformanceGoalsByEmpId(Long empId);

	@Select("select id, emp_id, cycle_id, goal_title, target_value, self_score, manager_score, final_score, final_level, "
			+ "status, reviewer_id, calibrated_at, created_at, updated_at from performance_goal "
			+ "where emp_id = #{empId} and final_score is not null order by calibrated_at desc, id desc limit 1")
	PerformanceGoal findLatestFinalGoal(Long empId);

	@Delete("delete from performance_goal where cycle_id = #{cycleId}")
	int deletePerformanceGoalsByCycleId(Long cycleId);

	@Insert("insert into performance_goal(emp_id, cycle_id, goal_title, target_value, status, reviewer_id, created_at, updated_at) "
			+ "values(#{empId}, #{cycleId}, #{goalTitle}, #{targetValue}, #{status}, #{reviewerId}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertPerformanceGoal(PerformanceGoal goal);

	@Update("update performance_goal set self_score = #{selfScore}, status = #{status}, updated_at = now() where id = #{id}")
	int updateSelfScore(PerformanceGoal goal);

	@Update("update performance_goal set manager_score = #{managerScore}, reviewer_id = #{reviewerId}, status = #{status}, updated_at = now() where id = #{id}")
	int updateManagerScore(PerformanceGoal goal);

	@Update("update performance_goal set final_score = #{finalScore}, final_level = #{finalLevel}, status = #{status}, "
			+ "calibrated_at = now(), updated_at = now() where id = #{id}")
	int calibrateGoal(PerformanceGoal goal);

	@Select("select id, emp_id, cycle_month, base_salary, allowance, bonus, overtime_pay, leave_deduction, social_security, "
			+ "housing_fund, tax, gross_salary, net_salary, performance_level, status, approver_id, assignee_user_id, approved_at, acknowledged_at, created_at, updated_at "
			+ "from payslip order by cycle_month desc, id desc")
	List<Payslip> listPayslips();

	@Select("select id, emp_id, cycle_month, base_salary, allowance, bonus, overtime_pay, leave_deduction, social_security, "
			+ "housing_fund, tax, gross_salary, net_salary, performance_level, status, approver_id, assignee_user_id, approved_at, acknowledged_at, created_at, updated_at "
			+ "from payslip where id = #{id}")
	Payslip findPayslip(Long id);

	@Select("select id, emp_id, cycle_month, base_salary, allowance, bonus, overtime_pay, leave_deduction, social_security, "
			+ "housing_fund, tax, gross_salary, net_salary, performance_level, status, approver_id, assignee_user_id, approved_at, acknowledged_at, created_at, updated_at "
			+ "from payslip where emp_id = #{empId} order by cycle_month desc, id desc")
	List<Payslip> listPayslipsByEmpId(Long empId);

	@Select("select id, emp_id, cycle_month, base_salary, allowance, bonus, overtime_pay, leave_deduction, social_security, "
			+ "housing_fund, tax, gross_salary, net_salary, performance_level, status, approver_id, assignee_user_id, approved_at, acknowledged_at, created_at, updated_at "
			+ "from payslip where emp_id = #{empId} and cycle_month = #{cycleMonth}")
	Payslip findPayslipByEmpAndMonth(@Param("empId") Long empId, @Param("cycleMonth") String cycleMonth);

	@Insert("insert into payslip(emp_id, cycle_month, base_salary, allowance, bonus, overtime_pay, leave_deduction, social_security, "
			+ "housing_fund, tax, gross_salary, net_salary, performance_level, status, assignee_user_id, acknowledged_at, created_at, updated_at) values(#{empId}, "
			+ "#{cycleMonth}, #{baseSalary}, #{allowance}, #{bonus}, #{overtimePay}, #{leaveDeduction}, #{socialSecurity}, "
			+ "#{housingFund}, #{tax}, #{grossSalary}, #{netSalary}, #{performanceLevel}, #{status}, #{assigneeUserId}, #{acknowledgedAt}, now(), now()) "
			+ "on duplicate key update base_salary = values(base_salary), allowance = values(allowance), bonus = values(bonus), "
			+ "overtime_pay = values(overtime_pay), leave_deduction = values(leave_deduction), social_security = values(social_security), "
			+ "housing_fund = values(housing_fund), tax = values(tax), gross_salary = values(gross_salary), net_salary = values(net_salary), "
			+ "performance_level = values(performance_level), status = values(status), assignee_user_id = values(assignee_user_id), acknowledged_at = values(acknowledged_at), updated_at = now()")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int upsertPayslip(Payslip payslip);

	@Update("update payslip set status = #{status}, approver_id = #{approverId}, approved_at = now(), updated_at = now() where id = #{id}")
	int updatePayslipStatus(Payslip payslip);

	@Update("update payslip set acknowledged_at = now(), status = #{status}, updated_at = now() where id = #{id}")
	int acknowledgePayslip(Payslip payslip);

	@Select("select id, payslip_id, component_type, amount, source_type, source_id, remark, created_at from payslip_component where payslip_id = #{payslipId} order by id asc")
	List<PayslipComponent> listPayslipComponents(Long payslipId);

	@Insert("insert into payslip_component(payslip_id, component_type, amount, source_type, source_id, remark, created_at) "
			+ "values(#{payslipId}, #{componentType}, #{amount}, #{sourceType}, #{sourceId}, #{remark}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertPayslipComponent(PayslipComponent component);

	@Update("delete from payslip_component where payslip_id = #{payslipId}")
	int deletePayslipComponents(Long payslipId);

	@Select("select id, emp_id, before_salary, after_salary, effective_month, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from salary_change order by id desc")
	List<SalaryChange> listSalaryChanges();

	@Select("select id, emp_id, before_salary, after_salary, effective_month, reason, status, approver_id, assignee_user_id, approved_at, created_at, updated_at "
			+ "from salary_change where id = #{id}")
	SalaryChange findSalaryChange(Long id);

	@Insert("insert into salary_change(emp_id, before_salary, after_salary, effective_month, reason, status, assignee_user_id, created_at, updated_at) "
			+ "values(#{empId}, #{beforeSalary}, #{afterSalary}, #{effectiveMonth}, #{reason}, #{status}, #{assigneeUserId}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertSalaryChange(SalaryChange change);

	@Update("update salary_change set status = #{status}, approver_id = #{approverId}, approved_at = now(), updated_at = now() where id = #{id}")
	int updateSalaryChangeStatus(SalaryChange change);

	@Update("update salary_structure set base_salary = #{baseSalary} where emp_id = #{empId}")
	int updateSalaryStructureBase(@Param("empId") Long empId, @Param("baseSalary") BigDecimal baseSalary);

	@Select("select id, user_name, emp_id, title, content, biz_type, biz_id, status, created_at from notification order by id desc limit 200")
	List<NotificationItem> listNotifications();

	@Select("select id, user_name, emp_id, title, content, biz_type, biz_id, status, created_at from notification "
			+ "where emp_id = #{empId} or user_name = #{userName} order by id desc limit 100")
	List<NotificationItem> listNotificationsForUser(@Param("empId") Long empId, @Param("userName") String userName);

	@Insert("insert into notification(user_name, emp_id, title, content, biz_type, biz_id, status, created_at) "
			+ "values(#{userName}, #{empId}, #{title}, #{content}, #{bizType}, #{bizId}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insertNotification(NotificationItem notification);

	@Update("update notification set status = 'READ' where id = #{id}")
	int markNotificationRead(Long id);

	@Select("select count(*) from notification where emp_id = #{empId} and biz_type = #{bizType} and biz_id = #{bizId} and title = #{title} and status = 'UNREAD' and date(created_at) = current_date()")
	int countUnreadNotificationByBiz(@Param("empId") Long empId, @Param("bizType") String bizType, @Param("bizId") Long bizId, @Param("title") String title);

	@Select("select '员工' as name, count(*) as value from employee union all "
			+ "select '部门', count(*) from department union all "
			+ "select '合同', count(*) from employee_contract union all "
			+ "select '待审批', count(*) from employee_lifecycle_event where status like 'PENDING%' union all "
			+ "select '工资条', count(*) from payslip union all "
			+ "select '通知', count(*) from notification")
	List<Map<String, Object>> enterpriseOverview();
}
