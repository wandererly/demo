package com.hrm.service;

import com.hrm.domain.PerformanceCycle;
import com.hrm.domain.PerformanceIndicator;
import com.hrm.domain.PerformanceReview;
import com.hrm.dto.PerformanceCycleCreateRequest;
import com.hrm.dto.PerformanceIndicatorCreateRequest;
import com.hrm.dto.PerformanceIndicatorUpdateRequest;
import com.hrm.dto.PerformanceReviewCreateRequest;
import com.hrm.dto.PerformanceReviewUpdateRequest;
import com.hrm.dto.PerformanceReviewApproveRequest;
import java.util.List;

public interface PerformanceService {
	List<PerformanceCycle> listCycles();
	PerformanceCycle getCycle(Long id);
	PerformanceCycle createCycle(PerformanceCycleCreateRequest request);
	PerformanceCycle updateCycle(Long id, PerformanceCycleCreateRequest request);
	void deleteCycle(Long id);

	List<PerformanceIndicator> listIndicators(Long cycleId);
	PerformanceIndicator createIndicator(PerformanceIndicatorCreateRequest request);
	PerformanceIndicator updateIndicator(Long id, PerformanceIndicatorUpdateRequest request);
	void deleteIndicator(Long id);

	List<PerformanceReview> listReviews(Long cycleId);
	PerformanceReview createReview(PerformanceReviewCreateRequest request);
	PerformanceReview updateReview(Long id, PerformanceReviewUpdateRequest request);
	PerformanceReview approveReview(Long id, PerformanceReviewApproveRequest request);
}





