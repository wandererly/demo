package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.PerformanceCycle;
import com.hrm.domain.PerformanceIndicator;
import com.hrm.domain.PerformanceReview;
import com.hrm.dto.PerformanceCycleCreateRequest;
import com.hrm.dto.PerformanceIndicatorCreateRequest;
import com.hrm.dto.PerformanceIndicatorUpdateRequest;
import com.hrm.dto.PerformanceReviewCreateRequest;
import com.hrm.dto.PerformanceReviewUpdateRequest;
import com.hrm.dto.PerformanceReviewApproveRequest;
import com.hrm.service.PerformanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
@Validated
@PreAuthorize("hasAuthority('perf:manage')")
public class PerformanceController {

	private final PerformanceService performanceService;

	public PerformanceController(PerformanceService performanceService) {
		this.performanceService = performanceService;
	}

	@GetMapping("/cycles")
	public ApiResponse<List<PerformanceCycle>> listCycles() {
		return ApiResponse.ok(performanceService.listCycles());
	}

	@GetMapping("/cycles/{id}")
	public ApiResponse<PerformanceCycle> getCycle(@PathVariable Long id) {
		return ApiResponse.ok(performanceService.getCycle(id));
	}

	@PostMapping("/cycles")
	public ApiResponse<PerformanceCycle> createCycle(@Valid @RequestBody PerformanceCycleCreateRequest request) {
		return ApiResponse.ok(performanceService.createCycle(request));
	}

	@PutMapping("/cycles/{id}")
	public ApiResponse<PerformanceCycle> updateCycle(@PathVariable Long id,
									  @Valid @RequestBody PerformanceCycleCreateRequest request) {
		return ApiResponse.ok(performanceService.updateCycle(id, request));
	}

	@DeleteMapping("/cycles/{id}")
	public ApiResponse<Void> deleteCycle(@PathVariable Long id) {
		performanceService.deleteCycle(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/cycles/{cycleId}/indicators")
	public ApiResponse<List<PerformanceIndicator>> listIndicators(@PathVariable Long cycleId) {
		return ApiResponse.ok(performanceService.listIndicators(cycleId));
	}

	@PostMapping("/indicators")
	public ApiResponse<PerformanceIndicator> createIndicator(@Valid @RequestBody PerformanceIndicatorCreateRequest request) {
		return ApiResponse.ok(performanceService.createIndicator(request));
	}

	@PutMapping("/indicators/{id}")
	public ApiResponse<PerformanceIndicator> updateIndicator(@PathVariable Long id,
									  @Valid @RequestBody PerformanceIndicatorUpdateRequest request) {
		return ApiResponse.ok(performanceService.updateIndicator(id, request));
	}

	@DeleteMapping("/indicators/{id}")
	public ApiResponse<Void> deleteIndicator(@PathVariable Long id) {
		performanceService.deleteIndicator(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/cycles/{cycleId}/reviews")
	public ApiResponse<List<PerformanceReview>> listReviews(@PathVariable Long cycleId) {
		return ApiResponse.ok(performanceService.listReviews(cycleId));
	}

	@PostMapping("/reviews")
	public ApiResponse<PerformanceReview> createReview(@Valid @RequestBody PerformanceReviewCreateRequest request) {
		return ApiResponse.ok(performanceService.createReview(request));
	}

	@PutMapping("/reviews/{id}")
	public ApiResponse<PerformanceReview> updateReview(@PathVariable Long id,
									  @Valid @RequestBody PerformanceReviewUpdateRequest request) {
		return ApiResponse.ok(performanceService.updateReview(id, request));
	}
	@PostMapping("/reviews/{id}/approve")
	public ApiResponse<PerformanceReview> approveReview(@PathVariable Long id,
											@RequestBody PerformanceReviewApproveRequest request) {
		return ApiResponse.ok(performanceService.approveReview(id, request));
	}
}




