package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.JobLevel;
import com.hrm.domain.JobPosition;
import com.hrm.domain.PromotionPath;
import com.hrm.mapper.JobLevelMapper;
import com.hrm.mapper.JobPositionMapper;
import com.hrm.mapper.PromotionPathMapper;
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

	public OrgController(JobLevelMapper jobLevelMapper,
					 JobPositionMapper jobPositionMapper,
					 PromotionPathMapper promotionPathMapper) {
		this.jobLevelMapper = jobLevelMapper;
		this.jobPositionMapper = jobPositionMapper;
		this.promotionPathMapper = promotionPathMapper;
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
		jobLevelMapper.insert(level);
		return ApiResponse.ok(level);
	}

	@PostMapping("/positions")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<JobPosition> createPosition(@RequestBody JobPosition position) {
		jobPositionMapper.insert(position);
		return ApiResponse.ok(position);
	}

	@PostMapping("/paths")
	@PreAuthorize("hasAuthority('hr:manage')")
	public ApiResponse<PromotionPath> createPath(@RequestBody PromotionPath path) {
		promotionPathMapper.insert(path);
		return ApiResponse.ok(path);
	}
}
