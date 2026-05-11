package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.AuditLog;
import com.hrm.service.AuditLogService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAuthority('audit:view') or hasAuthority('config:manage')")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping
	public ApiResponse<List<AuditLog>> listRecent() {
		return ApiResponse.ok(auditLogService.listRecent());
	}
}
