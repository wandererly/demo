package com.hrm.service;

import com.hrm.dto.ApprovalDecisionRequest;
import com.hrm.dto.ApprovalTask;
import java.util.List;

public interface ApprovalService {
	List<ApprovalTask> listPending();
	void approve(ApprovalDecisionRequest request);
}
