package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.JobLevel;
import com.hrm.domain.JobPosition;
import com.hrm.domain.PromotionPath;
import com.hrm.mapper.JobLevelMapper;
import com.hrm.mapper.JobPositionMapper;
import com.hrm.mapper.PromotionPathMapper;
import com.hrm.service.AuditLogService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/org")
public class OrgController {

	private final JobLevelMapper jobLevelMapper;
	private final JobPositionMapper jobPositionMapper;
	private final PromotionPathMapper promotionPathMapper;
	private final AuditLogService auditLogService;

	public OrgController(JobLevelMapper jobLevelMapper,
					 JobPositionMapper jobPositionMapper,
					 PromotionPathMapper promotionPathMapper,
					 AuditLogService auditLogService) {
		this.jobLevelMapper = jobLevelMapper;
		this.jobPositionMapper = jobPositionMapper;
		this.promotionPathMapper = promotionPathMapper;
		this.auditLogService = auditLogService;
	}

	@GetMapping("/levels")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<List<JobLevel>> levels() {
		return ApiResponse.ok(jobLevelMapper.findAll());
	}

	@GetMapping("/positions")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<List<JobPosition>> positions() {
		return ApiResponse.ok(jobPositionMapper.findAll());
	}

	@GetMapping("/paths")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<List<PromotionPath>> paths() {
		return ApiResponse.ok(promotionPathMapper.findAll());
	}

	@PostMapping("/levels")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<JobLevel> createLevel(@RequestBody JobLevel level) {
		if (level.getStatus() == null || level.getStatus().isBlank()) {
			level.setStatus("ACTIVE");
		}
		jobLevelMapper.insert(level);
		auditLogService.record("组织架构", "新增职级", "job_level", level.getId(), level.getLevelName());
		return ApiResponse.ok(level);
	}

	@PostMapping("/positions")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<JobPosition> createPosition(@RequestBody JobPosition position) {
		if (position.getStatus() == null || position.getStatus().isBlank()) {
			position.setStatus("ACTIVE");
		}
		jobPositionMapper.insert(position);
		auditLogService.record("组织架构", "新增岗位", "job_position", position.getId(), position.getPositionName());
		return ApiResponse.ok(position);
	}

	@PostMapping("/paths")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<PromotionPath> createPath(@RequestBody PromotionPath path) {
		promotionPathMapper.insert(path);
		auditLogService.record("组织架构", "新增晋升路径", "promotion_path", path.getId(), path.getConditionText());
		return ApiResponse.ok(path);
	}
}
