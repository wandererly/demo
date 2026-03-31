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
import com.hrm.service.LeaveService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LeaveServiceImpl implements LeaveService {

	private final LeaveRequestMapper leaveRequestMapper;
	private final LeaveRuleMapper leaveRuleMapper;

	public LeaveServiceImpl(LeaveRequestMapper leaveRequestMapper, LeaveRuleMapper leaveRuleMapper) {
		this.leaveRequestMapper = leaveRequestMapper;
		this.leaveRuleMapper = leaveRuleMapper;
	}

	@Override
	public List<LeaveRequest> list() {
		return leaveRequestMapper.findAll();
	}

	@Override
	public LeaveRequest get(Long id) {
		LeaveRequest request = leaveRequestMapper.findById(id);
		if (request == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Leave request not found");
		}
		return request;
	}

	@Override
	public LeaveRequest create(LeaveCreateRequest request) {
		LeaveRule rule = leaveRuleMapper.findLatest();
		if (rule != null && request.getDays() != null && request.getDays() > rule.getMaxDaysPerRequest()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "Leave days exceed rule limit");
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
		leaveRequestMapper.insert(entity);
		return entity;
	}

	@Override
	public LeaveRequest update(Long id, LeaveUpdateRequest request) {
		LeaveRequest existing = get(id);
		existing.setLeaveType(request.getLeaveType() == null ? existing.getLeaveType() : request.getLeaveType());
		existing.setStartTime(request.getStartTime() == null ? existing.getStartTime() : request.getStartTime());
		existing.setEndTime(request.getEndTime() == null ? existing.getEndTime() : request.getEndTime());
		existing.setDays(request.getDays() == null ? existing.getDays() : request.getDays());
		existing.setReason(request.getReason() == null ? existing.getReason() : request.getReason());
		existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
		existing.setApproverId(request.getApproverId() == null ? existing.getApproverId() : request.getApproverId());
		leaveRequestMapper.update(existing);
		return existing;
	}

	@Override
	public LeaveRequest approve(Long id, LeaveApproveRequest request) {
		LeaveRequest existing = get(id);
		LeaveRule rule = leaveRuleMapper.findLatest();
		String status = request.getStatus();
		if (rule != null && rule.getApprovalLevel() >= 2 && "APPROVED".equals(status)) {
			if ("PENDING_L1".equals(existing.getStatus())) {
				status = "PENDING_L2";
			}
		}
		existing.setApproverId(request.getApproverId());
		existing.setStatus(status);
		leaveRequestMapper.updateStatus(existing);
		return existing;
	}

	@Override
	public void delete(Long id) {
		int rows = leaveRequestMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Leave request not found");
		}
	}
}
