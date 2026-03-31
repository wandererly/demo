package com.hrm.mapper;

import com.hrm.domain.PerformanceReview;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PerformanceReviewMapper {

	@Select("select id, emp_id, cycle_id, score, level, reviewer_id, goal_summary, approval_status, approver_id, approved_at, comment, created_at from performance_review where cycle_id = #{cycleId} order by id desc")
	List<PerformanceReview> findByCycleId(Long cycleId);

	@Select("select id, emp_id, cycle_id, score, level, reviewer_id, goal_summary, approval_status, approver_id, approved_at, comment, created_at from performance_review where id = #{id}")
	PerformanceReview findById(Long id);

	@Insert("insert into performance_review(emp_id, cycle_id, score, level, reviewer_id, goal_summary, approval_status, approver_id, approved_at, comment, created_at) "
			+ "values(#{empId}, #{cycleId}, #{score}, #{level}, #{reviewerId}, #{goalSummary}, #{approvalStatus}, #{approverId}, #{approvedAt}, #{comment}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PerformanceReview review);

	@Update("update performance_review set score = #{score}, level = #{level}, reviewer_id = #{reviewerId}, goal_summary = #{goalSummary}, approval_status = #{approvalStatus}, approver_id = #{approverId}, approved_at = #{approvedAt}, comment = #{comment} where id = #{id}")
	int update(PerformanceReview review);
}

