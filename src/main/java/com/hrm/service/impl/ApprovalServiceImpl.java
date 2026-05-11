package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceCorrection;
import com.hrm.domain.Employee;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.OvertimeRequest;
import com.hrm.domain.Payslip;
import com.hrm.domain.PerformanceCycle;
import com.hrm.domain.PerformanceReview;
import com.hrm.domain.SalaryChange;
import com.hrm.domain.SysUser;
import com.hrm.dto.ApprovalDecisionRequest;
import com.hrm.dto.ApprovalTask;
import com.hrm.dto.LeaveApproveRequest;
import com.hrm.dto.PerformanceReviewApproveRequest;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.PerformanceCycleMapper;
import com.hrm.mapper.PerformanceReviewMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.service.ApprovalService;
import com.hrm.service.EnterpriseService;
import com.hrm.service.LeaveService;
import com.hrm.service.PerformanceService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ApprovalServiceImpl implements ApprovalService {

	private final LeaveRequestMapper leaveRequestMapper;
	private final PerformanceReviewMapper performanceReviewMapper;
	private final PerformanceCycleMapper performanceCycleMapper;
	private final EmployeeMapper employeeMapper;
	private final LeaveService leaveService;
	private final PerformanceService performanceService;
	private final EnterpriseService enterpriseService;
	private final SysUserMapper sysUserMapper;

	public ApprovalServiceImpl(LeaveRequestMapper leaveRequestMapper,
						   PerformanceReviewMapper performanceReviewMapper,
						   PerformanceCycleMapper performanceCycleMapper,
						   EmployeeMapper employeeMapper,
						   LeaveService leaveService,
						   PerformanceService performanceService,
						   EnterpriseService enterpriseService,
						   SysUserMapper sysUserMapper) {
		this.leaveRequestMapper = leaveRequestMapper;
		this.performanceReviewMapper = performanceReviewMapper;
		this.performanceCycleMapper = performanceCycleMapper;
		this.employeeMapper = employeeMapper;
		this.leaveService = leaveService;
		this.performanceService = performanceService;
		this.enterpriseService = enterpriseService;
		this.sysUserMapper = sysUserMapper;
	}

	@Override
	public List<ApprovalTask> listPending() {
		List<ApprovalTask> tasks = new ArrayList<>();
		for (LeaveRequest request : leaveRequestMapper.findAll()) {
			if (isPending(request.getStatus())) {
				Employee employee = employeeMapper.findById(request.getEmpId());
				ApprovalTask task = baseTask("LEAVE", request.getId(), request.getEmpId(), employee);
				task.setTitle(request.getLeaveType() + " / " + request.getDays() + "天");
				task.setStatus(request.getStatus());
				task.setApproverId(request.getApproverId());
				task.setSource("请假");
				task.setCreatedAt(request.getCreatedAt());
				tasks.add(task);
			}
		}
		for (PerformanceReview review : performanceReviewMapper.findAll()) {
			if (isPending(review.getApprovalStatus())) {
				Employee employee = employeeMapper.findById(review.getEmpId());
				PerformanceCycle cycle = performanceCycleMapper.findById(review.getCycleId());
				ApprovalTask task = baseTask("PERFORMANCE", review.getId(), review.getEmpId(), employee);
				task.setTitle((cycle == null ? "绩效评审" : cycle.getName()) + " / " + review.getScore() + "分");
				task.setStatus(review.getApprovalStatus());
				task.setApproverId(review.getApproverId());
				task.setSource("绩效");
				task.setCreatedAt(review.getCreatedAt());
				tasks.add(task);
			}
		}
		for (LifecycleEvent event : enterpriseService.listLifecycleEvents()) {
			if (isPending(event.getStatus())) {
				Employee employee = employeeMapper.findById(event.getEmpId());
				ApprovalTask task = baseTask(event.getEventType(), event.getId(), event.getEmpId(), employee);
				task.setTitle(event.getTitle());
				task.setStatus(event.getStatus());
				task.setApproverId(event.getApproverId());
				task.setSource("员工生命周期");
				task.setCreatedAt(event.getCreatedAt());
				tasks.add(task);
			}
		}
		for (AttendanceCorrection correction : enterpriseService.listCorrections()) {
			if (isPending(correction.getStatus())) {
				Employee employee = employeeMapper.findById(correction.getEmpId());
				ApprovalTask task = baseTask("ATTENDANCE_CORRECTION", correction.getId(), correction.getEmpId(), employee);
				task.setTitle(correction.getWorkDate() + " 补卡");
				task.setStatus(correction.getStatus());
				task.setApproverId(correction.getApproverId());
				task.setSource("补卡申请");
				task.setCreatedAt(correction.getCreatedAt());
				tasks.add(task);
			}
		}
		for (OvertimeRequest overtime : enterpriseService.listOvertimeRequests()) {
			if (isPending(overtime.getStatus())) {
				Employee employee = employeeMapper.findById(overtime.getEmpId());
				ApprovalTask task = baseTask("OVERTIME", overtime.getId(), overtime.getEmpId(), employee);
				task.setTitle(overtime.getWorkDate() + " / " + overtime.getHours() + "小时");
				task.setStatus(overtime.getStatus());
				task.setApproverId(overtime.getApproverId());
				task.setSource("加班申请");
				task.setCreatedAt(overtime.getCreatedAt());
				tasks.add(task);
			}
		}
		for (Payslip payslip : enterpriseService.listPayslips()) {
			if (isPending(payslip.getStatus())) {
				Employee employee = employeeMapper.findById(payslip.getEmpId());
				ApprovalTask task = baseTask("PAYROLL", payslip.getId(), payslip.getEmpId(), employee);
				task.setTitle(payslip.getCycleMonth() + " 工资条 / 实发 " + payslip.getNetSalary());
				task.setStatus(payslip.getStatus());
				task.setApproverId(payslip.getApproverId());
				task.setSource("薪酬审批");
				task.setCreatedAt(payslip.getCreatedAt());
				tasks.add(task);
			}
		}
		for (SalaryChange change : enterpriseService.listSalaryChanges()) {
			if (isPending(change.getStatus())) {
				Employee employee = employeeMapper.findById(change.getEmpId());
				ApprovalTask task = baseTask("SALARY_CHANGE", change.getId(), change.getEmpId(), employee);
				task.setTitle(change.getEffectiveMonth() + " 调薪 / " + change.getBeforeSalary() + " -> " + change.getAfterSalary());
				task.setStatus(change.getStatus());
				task.setApproverId(change.getApproverId());
				task.setSource("调薪审批");
				task.setCreatedAt(change.getCreatedAt());
				tasks.add(task);
			}
		}
		tasks.sort((a, b) -> {
			if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
			if (a.getCreatedAt() == null) return 1;
			if (b.getCreatedAt() == null) return -1;
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		});
		return tasks;
	}

	@Override
	public void approve(ApprovalDecisionRequest request) {
		Long approverId = currentApproverId();
		if ("LEAVE".equalsIgnoreCase(request.getType())) {
			LeaveApproveRequest approveRequest = new LeaveApproveRequest();
			approveRequest.setApproverId(approverId);
			approveRequest.setStatus(request.getStatus());
			LeaveRequest approved = leaveService.approve(request.getBusinessId(), approveRequest);
			if ("APPROVED".equals(approved.getStatus())) {
				enterpriseService.applyLeaveBalance(approved.getId());
			}
			return;
		}
		if ("PERFORMANCE".equalsIgnoreCase(request.getType())) {
			PerformanceReviewApproveRequest approveRequest = new PerformanceReviewApproveRequest();
			approveRequest.setApproverId(approverId);
			approveRequest.setApprovalStatus(request.getStatus());
			approveRequest.setComment(request.getComment());
			performanceService.approveReview(request.getBusinessId(), approveRequest);
			return;
		}
		if (isLifecycleType(request.getType())) {
			enterpriseService.approveLifecycle(request.getBusinessId(), approverId, request.getStatus());
			return;
		}
		if ("ATTENDANCE_CORRECTION".equalsIgnoreCase(request.getType())) {
			enterpriseService.approveCorrection(request.getBusinessId(), approverId, request.getStatus());
			return;
		}
		if ("OVERTIME".equalsIgnoreCase(request.getType())) {
			enterpriseService.approveOvertime(request.getBusinessId(), approverId, request.getStatus());
			return;
		}
		if ("PAYROLL".equalsIgnoreCase(request.getType())) {
			enterpriseService.approvePayslip(request.getBusinessId(), approverId, request.getStatus());
			return;
		}
		if ("SALARY_CHANGE".equalsIgnoreCase(request.getType())) {
			enterpriseService.approveSalaryChange(request.getBusinessId(), approverId, request.getStatus());
			return;
		}
		throw new BizException(ErrorCode.BAD_REQUEST, "不支持的审批类型");
	}

	private Long currentApproverId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal() == null
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			throw new BizException(ErrorCode.FORBIDDEN, "请先登录后再审批");
		}
		String username = String.valueOf(authentication.getPrincipal());
		SysUser user = sysUserMapper.findByUsername(username);
		if (user == null) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前登录用户不存在，不能审批");
		}
		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号已停用，不能审批");
		}
		return user.getId();
	}

	private boolean isPending(String status) {
		return "PENDING".equals(status) || "PENDING_L1".equals(status) || "PENDING_L2".equals(status);
	}

	private boolean isLifecycleType(String type) {
		return "REGULARIZATION".equalsIgnoreCase(type)
				|| "TRANSFER".equalsIgnoreCase(type)
				|| "RESIGNATION".equalsIgnoreCase(type);
	}

	private ApprovalTask baseTask(String type, Long businessId, Long applicantId, Employee employee) {
		ApprovalTask task = new ApprovalTask();
		task.setType(type);
		task.setBusinessId(businessId);
		task.setApplicantId(applicantId);
		task.setApplicantName(employee == null ? "-" : employee.getName());
		return task;
	}
}
