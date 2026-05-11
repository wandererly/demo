package com.hrm.mapper;

import com.hrm.domain.LeaveRequest;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LeaveRequestMapper {

	@Select("select id, emp_id, leave_type, start_time, end_time, days, reason, status, approver_id, created_at "
			+ "from leave_request order by id desc")
	List<LeaveRequest> findAll();

	@Select("select id, emp_id, leave_type, start_time, end_time, days, reason, status, approver_id, created_at "
			+ "from leave_request where id = #{id}")
	LeaveRequest findById(Long id);

	@Select("select id, emp_id, leave_type, start_time, end_time, days, reason, status, approver_id, created_at "
			+ "from leave_request where emp_id = #{empId} order by id desc")
	List<LeaveRequest> findByEmpId(Long empId);

	@Insert("insert into leave_request(emp_id, leave_type, start_time, end_time, days, reason, status, approver_id, created_at) "
			+ "values(#{empId}, #{leaveType}, #{startTime}, #{endTime}, #{days}, #{reason}, #{status}, #{approverId}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(LeaveRequest request);

	@Update("update leave_request set leave_type = #{leaveType}, start_time = #{startTime}, end_time = #{endTime}, days = #{days}, "
			+ "reason = #{reason}, status = #{status}, approver_id = #{approverId} where id = #{id}")
	int update(LeaveRequest request);

	@Update("update leave_request set status = #{status}, approver_id = #{approverId} where id = #{id}")
	int updateStatus(LeaveRequest request);

	@Delete("delete from leave_request where id = #{id}")
	int delete(Long id);
}
