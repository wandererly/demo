package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.dto.ApprovalDecisionRequest;
import com.hrm.dto.ApprovalTask;
import com.hrm.service.ApprovalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
@PreAuthorize("hasAuthority('approval:manage')")
public class ApprovalController {

	private final ApprovalService approvalService;

	public ApprovalController(ApprovalService approvalService) {
		this.approvalService = approvalService;
	}

	@GetMapping
	public ApiResponse<List<ApprovalTask>> listPending() {
		return ApiResponse.ok(approvalService.listPending());
	}

	@PostMapping("/decision")
	public ApiResponse<Void> approve(@Valid @RequestBody ApprovalDecisionRequest request) {
		approvalService.approve(request);
		return ApiResponse.ok(null);
	}
}
