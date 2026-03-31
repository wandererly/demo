package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.PerformanceCycle;
import com.hrm.domain.PerformanceIndicator;
import com.hrm.domain.PerformanceReview;
import com.hrm.domain.PerformanceRule;
import com.hrm.dto.PerformanceCycleCreateRequest;
import com.hrm.dto.PerformanceIndicatorCreateRequest;
import com.hrm.dto.PerformanceIndicatorUpdateRequest;
import com.hrm.dto.PerformanceReviewCreateRequest;
import com.hrm.dto.PerformanceReviewUpdateRequest;
import com.hrm.dto.PerformanceReviewApproveRequest;
import com.hrm.mapper.PerformanceCycleMapper;
import com.hrm.mapper.PerformanceIndicatorMapper;
import com.hrm.mapper.PerformanceReviewMapper;
import com.hrm.mapper.PerformanceRuleMapper;
import com.hrm.service.PerformanceService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PerformanceServiceImpl implements PerformanceService {

	private final PerformanceCycleMapper cycleMapper;
	private final PerformanceIndicatorMapper indicatorMapper;
	private final PerformanceReviewMapper reviewMapper;
	private final PerformanceRuleMapper ruleMapper;

	public PerformanceServiceImpl(PerformanceCycleMapper cycleMapper,
							  PerformanceIndicatorMapper indicatorMapper,
							  PerformanceReviewMapper reviewMapper,
							  PerformanceRuleMapper ruleMapper) {
		this.cycleMapper = cycleMapper;
		this.indicatorMapper = indicatorMapper;
		this.reviewMapper = reviewMapper;
		this.ruleMapper = ruleMapper;
	}

	@Override
	public List<PerformanceCycle> listCycles() {
		return cycleMapper.findAll();
	}

	@Override
	public PerformanceCycle getCycle(Long id) {
		PerformanceCycle cycle = cycleMapper.findById(id);
		if (cycle == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance cycle not found");
		}
		return cycle;
	}

	@Override
	public PerformanceCycle createCycle(PerformanceCycleCreateRequest request) {
		PerformanceCycle cycle = new PerformanceCycle();
		cycle.setName(request.getName());
		cycle.setStartDate(request.getStartDate());
		cycle.setEndDate(request.getEndDate());
		cycle.setStatus("OPEN");
		cycleMapper.insert(cycle);
		return cycle;
	}

	@Override
	public PerformanceCycle updateCycle(Long id, PerformanceCycleCreateRequest request) {
		PerformanceCycle cycle = getCycle(id);
		cycle.setName(request.getName());
		cycle.setStartDate(request.getStartDate());
		cycle.setEndDate(request.getEndDate());
		cycleMapper.update(cycle);
		return cycle;
	}

	@Override
	public void deleteCycle(Long id) {
		int rows = cycleMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance cycle not found");
		}
	}

	@Override
	public List<PerformanceIndicator> listIndicators(Long cycleId) {
		return indicatorMapper.findByCycleId(cycleId);
	}

	@Override
	public PerformanceIndicator createIndicator(PerformanceIndicatorCreateRequest request) {
		PerformanceIndicator indicator = new PerformanceIndicator();
		indicator.setCycleId(request.getCycleId());
		indicator.setName(request.getName());
		indicator.setWeight(request.getWeight());
		indicator.setDescription(request.getDescription());
		indicatorMapper.insert(indicator);
		return indicator;
	}

	@Override
	public PerformanceIndicator updateIndicator(Long id, PerformanceIndicatorUpdateRequest request) {
		PerformanceIndicator indicator = new PerformanceIndicator();
		indicator.setId(id);
		indicator.setName(request.getName());
		indicator.setWeight(request.getWeight());
		indicator.setDescription(request.getDescription());
		indicatorMapper.update(indicator);
		return indicator;
	}

	@Override
	public void deleteIndicator(Long id) {
		int rows = indicatorMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance indicator not found");
		}
	}

	@Override
	public List<PerformanceReview> listReviews(Long cycleId) {
		return reviewMapper.findByCycleId(cycleId);
	}

	@Override
	public PerformanceReview createReview(PerformanceReviewCreateRequest request) {
		PerformanceReview review = new PerformanceReview();
		review.setEmpId(request.getEmpId());
		review.setCycleId(request.getCycleId());
		review.setScore(request.getScore());
		review.setReviewerId(request.getReviewerId());
		review.setGoalSummary(request.getGoalSummary());
		String approvalStatus = request.getApprovalStatus();
		review.setApprovalStatus((approvalStatus == null || approvalStatus.isBlank()) ? "PENDING" : approvalStatus);
		review.setApproverId(request.getApproverId());
		review.setComment(request.getComment());

		String level = request.getLevel();
		if (level == null || level.isBlank()) {
			List<PerformanceRule> rules = ruleMapper.findAll();
			for (PerformanceRule rule : rules) {
				if (request.getScore() >= rule.getMinScore() && request.getScore() <= rule.getMaxScore()) {
					level = rule.getLevel();
					break;
				}
			}
		}
		review.setLevel(level == null ? "UNSET" : level);
		reviewMapper.insert(review);
		return review;
	}

	@Override
	public PerformanceReview updateReview(Long id, PerformanceReviewUpdateRequest request) {
		PerformanceReview existing = reviewMapper.findById(id);
		if (existing == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance review not found");
		}
		existing.setScore(request.getScore() == null ? existing.getScore() : request.getScore());
		existing.setLevel(request.getLevel() == null ? existing.getLevel() : request.getLevel());
		existing.setReviewerId(request.getReviewerId() == null ? existing.getReviewerId() : request.getReviewerId());
		existing.setGoalSummary(request.getGoalSummary() == null ? existing.getGoalSummary() : request.getGoalSummary());
		existing.setApprovalStatus(request.getApprovalStatus() == null ? existing.getApprovalStatus() : request.getApprovalStatus());
		existing.setApproverId(request.getApproverId() == null ? existing.getApproverId() : request.getApproverId());
		existing.setComment(request.getComment() == null ? existing.getComment() : request.getComment());
		reviewMapper.update(existing);
		return existing;
	}
	@Override
	public PerformanceReview approveReview(Long id, PerformanceReviewApproveRequest request) {
		PerformanceReview existing = reviewMapper.findById(id);
		if (existing == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance review not found");
		}
		String status = request.getApprovalStatus();
		existing.setApprovalStatus(status == null ? "APPROVED" : status);
		existing.setApproverId(request.getApproverId());
		existing.setApprovedAt(java.time.LocalDateTime.now());
		if (request.getComment() != null && !request.getComment().isBlank()) {
			existing.setComment(request.getComment());
		}
		reviewMapper.update(existing);
		return existing;
	}
}

