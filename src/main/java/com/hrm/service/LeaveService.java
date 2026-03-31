package com.hrm.service;

import com.hrm.domain.LeaveRequest;
import com.hrm.dto.LeaveApproveRequest;
import com.hrm.dto.LeaveCreateRequest;
import com.hrm.dto.LeaveUpdateRequest;
import java.util.List;

public interface LeaveService {
	List<LeaveRequest> list();
	LeaveRequest get(Long id);
	LeaveRequest create(LeaveCreateRequest request);
	LeaveRequest update(Long id, LeaveUpdateRequest request);
	LeaveRequest approve(Long id, LeaveApproveRequest request);
	void delete(Long id);
}
