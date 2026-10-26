package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.LeaveRule;
import com.hrm.dto.LeaveApproveRequest;
import com.hrm.dto.LeaveCreateRequest;
import com.hrm.dto.LeaveUpdateRequest;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.LeaveRuleMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.CurrentUserService;
import com.hrm.service.LeaveService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
/** 处理请假申请、审批状态流转和员工数据范围校验。 */
public class LeaveServiceImpl implements LeaveService {

	private final LeaveRequestMapper leaveRequestMapper;
	private final LeaveRuleMapper leaveRuleMapper;
	private final AuditLogService auditLogService;
	private final CurrentUserService currentUserService;

	public LeaveServiceImpl(LeaveRequestMapper leaveRequestMapper, LeaveRuleMapper leaveRuleMapper,
						 AuditLogService auditLogService,
						 CurrentUserService currentUserService) {
		this.leaveRequestMapper = leaveRequestMapper;
		this.leaveRuleMapper = leaveRuleMapper;
		this.auditLogService = auditLogService;
		this.currentUserService = currentUserService;
	}

	@Override
	public List<LeaveRequest> list() {
		List<LeaveRequest> rows = new ArrayList<>();
		for (LeaveRequest request : leaveRequestMapper.findAll()) {
			if (currentUserService.canAccessEmployee(request.getEmpId())) {
				rows.add(request);
			}
		}
		return rows;
	}

	@Override
	public LeaveRequest get(Long id) {
		LeaveRequest request = leaveRequestMapper.findById(id);
		if (request == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "请假单不存在");
		}
		if (!currentUserService.canAccessEmployee(request.getEmpId()) && !currentUserService.hasPermission("approval:manage")) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权查看该请假单");
		}
		return request;
	}

	@Override
	public LeaveRequest create(LeaveCreateRequest request) {
		// 创建前校验日期、天数和规则上限，并自动设置审批状态及分派人。
		currentUserService.requireAccessibleEmployee(request.getEmpId());
		if (request.getStartTime() == null || request.getEndTime() == null || !request.getEndTime().isAfter(request.getStartTime())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "结束时间必须晚于开始时间");
		}
		if (request.getDays() == null || request.getDays() <= 0) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请假天数必须大于0");
		}
		LeaveRule rule = leaveRuleMapper.findLatest();
		if (rule != null && request.getDays() != null && request.getDays() > rule.getMaxDaysPerRequest()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请假天数超过规则限制");
		}

		LeaveRequest entity = new LeaveRequest();
		entity.setEmpId(request.getEmpId());
		entity.setLeaveType(request.getLeaveType());
		entity.setStartTime(request.getStartTime());
		entity.setEndTime(request.getEndTime());
		entity.setDays(request.getDays());
		entity.setReason(request.getReason());

		if (rule != null && rule.getApprovalLevel() >= 2) {
			entity.setStatus("PENDING_L1");
		} else {
			entity.setStatus("PENDING");
		}
		entity.setAssigneeUserId(currentUserService.assigneeForEmployee(request.getEmpId()));
		leaveRequestMapper.insert(entity);
		auditLogService.record("请假", "提交", "leave_request", entity.getId(), "员工ID " + entity.getEmpId());
		return entity;
	}

	@Override
	public LeaveRequest update(Long id, LeaveUpdateRequest request) {
		LeaveRequest existing = get(id);
		if ("APPROVED".equals(existing.getStatus()) || "REJECTED".equals(existing.getStatus())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "已完成审批的请假单不能修改");
		}
		existing.setLeaveType(request.getLeaveType() == null ? existing.getLeaveType() : request.getLeaveType());
		existing.setStartTime(request.getStartTime() == null ? existing.getStartTime() : request.getStartTime());
		existing.setEndTime(request.getEndTime() == null ? existing.getEndTime() : request.getEndTime());
		existing.setDays(request.getDays() == null ? existing.getDays() : request.getDays());
		existing.setReason(request.getReason() == null ? existing.getReason() : request.getReason());
		existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
		leaveRequestMapper.update(existing);
		auditLogService.record("请假", "更新", "leave_request", existing.getId(), "状态 " + existing.getStatus());
		return existing;
	}

	@Override
	public LeaveRequest approve(Long id, LeaveApproveRequest request) {
		// 已完成的审批不可重复处理；多级审批通过一级后转入下一级待办。
		LeaveRequest existing = get(id);
		if ("APPROVED".equals(existing.getStatus()) || "REJECTED".equals(existing.getStatus())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "该请假单已经完成审批，不能重复处理");
		}
		LeaveRule rule = leaveRuleMapper.findLatest();
		String status = request.getStatus();
		if (status == null || status.isBlank()) {
			status = "APPROVED";
		}
		if (rule != null && rule.getApprovalLevel() >= 2 && "APPROVED".equals(status)) {
			if ("PENDING_L1".equals(existing.getStatus())) {
				status = "PENDING_L2";
			}
		}
		if (!canApproveAssigned(existing.getAssigneeUserId())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号无权审批该请假单");
		}
		existing.setApproverId(currentUserService.requireCurrentUser().getId());
		existing.setStatus(status);
		if ("PENDING_L2".equals(status)) {
			Long fallback = currentUserService.firstApprovalManagerUserId();
			existing.setAssigneeUserId(fallback);
			leaveRequestMapper.update(existing);
		}
		leaveRequestMapper.updateStatus(existing);
		auditLogService.record("请假", "审批", "leave_request", existing.getId(), "状态 " + existing.getStatus());
		return existing;
	}

	private boolean canApproveAssigned(Long assigneeUserId) {
		Long currentUserId = currentUserService.requireCurrentUser().getId();
		return currentUserService.hasPermission("approval:manage")
				|| (assigneeUserId != null && assigneeUserId.equals(currentUserId));
	}

	@Override
	public void delete(Long id) {
		LeaveRequest existing = get(id);
		if ("APPROVED".equals(existing.getStatus())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "已审批通过的请假单不能删除");
		}
		int rows = leaveRequestMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "请假单不存在");
		}
		auditLogService.record("请假", "删除", "leave_request", id, null);
	}
}
