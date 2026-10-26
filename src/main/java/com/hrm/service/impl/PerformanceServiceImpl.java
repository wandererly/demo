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
import com.hrm.mapper.EnterpriseMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.CurrentUserService;
import com.hrm.service.PerformanceService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerformanceServiceImpl implements PerformanceService {

	private final PerformanceCycleMapper cycleMapper;
	private final PerformanceIndicatorMapper indicatorMapper;
	private final PerformanceReviewMapper reviewMapper;
	private final PerformanceRuleMapper ruleMapper;
	private final EnterpriseMapper enterpriseMapper;
	private final AuditLogService auditLogService;
	private final CurrentUserService currentUserService;

	public PerformanceServiceImpl(PerformanceCycleMapper cycleMapper,
							  PerformanceIndicatorMapper indicatorMapper,
							  PerformanceReviewMapper reviewMapper,
							  PerformanceRuleMapper ruleMapper,
							  EnterpriseMapper enterpriseMapper,
							  AuditLogService auditLogService,
							  CurrentUserService currentUserService) {
		this.cycleMapper = cycleMapper;
		this.indicatorMapper = indicatorMapper;
		this.reviewMapper = reviewMapper;
		this.ruleMapper = ruleMapper;
		this.enterpriseMapper = enterpriseMapper;
		this.auditLogService = auditLogService;
		this.currentUserService = currentUserService;
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
		auditLogService.record("绩效", "新增周期", "performance_cycle", cycle.getId(), cycle.getName());
		return cycle;
	}

	@Override
	public PerformanceCycle updateCycle(Long id, PerformanceCycleCreateRequest request) {
		PerformanceCycle cycle = getCycle(id);
		cycle.setName(request.getName());
		cycle.setStartDate(request.getStartDate());
		cycle.setEndDate(request.getEndDate());
		cycleMapper.update(cycle);
		auditLogService.record("绩效", "更新周期", "performance_cycle", cycle.getId(), cycle.getName());
		return cycle;
	}

	@Override
	@Transactional
	public void deleteCycle(Long id) {
		getCycle(id);
		enterpriseMapper.deletePerformanceGoalsByCycleId(id);
		reviewMapper.deleteByCycleId(id);
		indicatorMapper.deleteByCycleId(id);
		int rows = cycleMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance cycle not found");
		}
		auditLogService.record("绩效", "删除周期", "performance_cycle", id, null);
	}

	@Override
	public List<PerformanceIndicator> listIndicators(Long cycleId) {
		return indicatorMapper.findByCycleId(cycleId);
	}

	@Override
	public PerformanceIndicator createIndicator(PerformanceIndicatorCreateRequest request) {
		String name = normalizeIndicatorName(request.getName());
		validateIndicator(request.getCycleId(), name, request.getWeight(), null);
		PerformanceIndicator indicator = new PerformanceIndicator();
		indicator.setCycleId(request.getCycleId());
		indicator.setName(name);
		indicator.setWeight(request.getWeight());
		indicator.setDescription(request.getDescription());
		indicatorMapper.insert(indicator);
		auditLogService.record("绩效", "新增指标", "performance_indicator", indicator.getId(), indicator.getName());
		return indicator;
	}

	@Override
	public PerformanceIndicator updateIndicator(Long id, PerformanceIndicatorUpdateRequest request) {
		PerformanceIndicator existing = indicatorMapper.findById(id);
		if (existing == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance indicator not found");
		}
		String name = request.getName() == null ? existing.getName() : normalizeIndicatorName(request.getName());
		Integer weight = request.getWeight() == null ? existing.getWeight() : request.getWeight();
		validateIndicator(existing.getCycleId(), name, weight, id);
		PerformanceIndicator indicator = new PerformanceIndicator();
		indicator.setId(id);
		indicator.setName(name);
		indicator.setWeight(weight);
		indicator.setDescription(request.getDescription() == null ? existing.getDescription() : request.getDescription());
		indicatorMapper.update(indicator);
		auditLogService.record("绩效", "更新指标", "performance_indicator", indicator.getId(), indicator.getName());
		return indicator;
	}

	@Override
	public void deleteIndicator(Long id) {
		int rows = indicatorMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Performance indicator not found");
		}
		auditLogService.record("绩效", "删除指标", "performance_indicator", id, null);
	}

	@Override
	public List<PerformanceReview> listReviews(Long cycleId) {
		List<PerformanceReview> rows = new ArrayList<>();
		for (PerformanceReview review : reviewMapper.findByCycleId(cycleId)) {
			if (currentUserService.canAccessEmployee(review.getEmpId()) || currentUserService.hasPermission("approval:manage")) {
				rows.add(review);
			}
		}
		return rows;
	}

	@Override
	public PerformanceReview createReview(PerformanceReviewCreateRequest request) {
		currentUserService.requireAccessibleEmployee(request.getEmpId());
		PerformanceReview review = new PerformanceReview();
		review.setEmpId(request.getEmpId());
		review.setCycleId(request.getCycleId());
		review.setScore(request.getScore());
		review.setReviewerId(request.getReviewerId());
		review.setGoalSummary(request.getGoalSummary());
		String approvalStatus = request.getApprovalStatus();
		review.setApprovalStatus(normalizeReviewStatus(approvalStatus, "PENDING"));
		review.setComment(request.getComment());
		review.setAssigneeUserId(currentUserService.assigneeForEmployee(request.getEmpId()));

		String level = request.getLevel();
		if (level == null || level.isBlank()) {
			List<PerformanceRule> rules = ruleMapper.findAll();
			for (PerformanceRule rule : rules) {
				if (request.getScore() != null && request.getScore() >= rule.getMinScore() && request.getScore() <= rule.getMaxScore()) {
					level = rule.getLevel();
					break;
				}
			}
		}
		review.setLevel(level == null ? "UNSET" : level);
		reviewMapper.insert(review);
		auditLogService.record("绩效", "新增评审", "performance_review", review.getId(), "员工ID " + review.getEmpId());
		return review;
	}

	@Override
	public PerformanceReview updateReview(Long id, PerformanceReviewUpdateRequest request) {
		PerformanceReview existing = reviewMapper.findById(id);
		if (existing == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "绩效评审不存在");
		}
		if (!currentUserService.canAccessEmployee(existing.getEmpId()) && !currentUserService.hasPermission("approval:manage")) {
			throw new BizException(ErrorCode.FORBIDDEN, "无权修改该绩效评审");
		}
		existing.setScore(request.getScore() == null ? existing.getScore() : request.getScore());
		existing.setLevel(request.getLevel() == null ? existing.getLevel() : request.getLevel());
		existing.setReviewerId(request.getReviewerId() == null ? existing.getReviewerId() : request.getReviewerId());
		existing.setGoalSummary(request.getGoalSummary() == null ? existing.getGoalSummary() : request.getGoalSummary());
		existing.setApprovalStatus(request.getApprovalStatus() == null
				? existing.getApprovalStatus()
				: normalizeReviewStatus(request.getApprovalStatus(), existing.getApprovalStatus()));
		existing.setComment(request.getComment() == null ? existing.getComment() : request.getComment());
		reviewMapper.update(existing);
		auditLogService.record("绩效", "更新评审", "performance_review", existing.getId(), "状态 " + existing.getApprovalStatus());
		return existing;
	}
	@Override
	public PerformanceReview approveReview(Long id, PerformanceReviewApproveRequest request) {
		PerformanceReview existing = reviewMapper.findById(id);
		if (existing == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "绩效评审不存在");
		}
		if (!canApproveAssigned(existing.getAssigneeUserId())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号无权审批该绩效评审");
		}
		String status = normalizeDecisionStatus(request.getApprovalStatus());
		existing.setApprovalStatus(status);
		existing.setApproverId(currentUserService.requireCurrentUser().getId());
		existing.setApprovedAt(java.time.LocalDateTime.now());
		if (request.getComment() != null && !request.getComment().isBlank()) {
			existing.setComment(request.getComment());
		}
		reviewMapper.update(existing);
		auditLogService.record("绩效", "审批评审", "performance_review", existing.getId(), "状态 " + existing.getApprovalStatus());
		return existing;
	}

	private boolean canApproveAssigned(Long assigneeUserId) {
		Long currentUserId = currentUserService.requireCurrentUser().getId();
		return assigneeUserId == null
				? currentUserService.hasPermission("approval:manage")
				: assigneeUserId.equals(currentUserId);
	}

	private String normalizeIndicatorName(String name) {
		if (name == null || name.isBlank()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "指标名称不能为空");
		}
		return name.trim();
	}

	private void validateIndicator(Long cycleId, String name, Integer weight, Long excludeId) {
		getCycle(cycleId);
		if (weight == null || weight < 0 || weight > 100) {
			throw new BizException(ErrorCode.BAD_REQUEST, "指标权重必须在0到100之间");
		}
		for (PerformanceIndicator item : indicatorMapper.findByCycleId(cycleId)) {
			if (!item.getId().equals(excludeId) && item.getName() != null
					&& item.getName().trim().equalsIgnoreCase(name)) {
				throw new BizException(ErrorCode.BAD_REQUEST, "同一绩效周期内指标名称不能重复");
			}
		}
		int totalWeight = indicatorMapper.sumWeightByCycleId(cycleId, excludeId);
		if (totalWeight + weight > 100) {
			throw new BizException(ErrorCode.BAD_REQUEST, "同一绩效周期内指标权重总和不能超过100%");
		}
	}

	private String normalizeReviewStatus(String value, String defaultValue) {
		String status = value == null || value.isBlank() ? defaultValue : value.trim().toUpperCase(Locale.ROOT);
		if (!List.of("PENDING", "PENDING_L1", "PENDING_L2", "APPROVED", "REJECTED").contains(status)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "无效的绩效审批状态");
		}
		return status;
	}

	private String normalizeDecisionStatus(String value) {
		String status = value == null || value.isBlank() ? "APPROVED" : value.trim().toUpperCase(Locale.ROOT);
		if (!List.of("APPROVED", "REJECTED").contains(status)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "审批状态只能是通过或驳回");
		}
		return status;
	}
}

