package com.hrm.mapper;

import com.hrm.domain.AttendanceRecord;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AttendanceRecordMapper {

	@Select("select id, emp_id, work_date, check_in, check_out, status, created_at from attendance_record order by id desc")
	List<AttendanceRecord> findAll();

	@Select("select id, emp_id, work_date, check_in, check_out, status, created_at from attendance_record where id = #{id}")
	AttendanceRecord findById(Long id);

	@Insert("insert into attendance_record(emp_id, work_date, check_in, check_out, status, created_at) "
			+ "values(#{empId}, #{workDate}, #{checkIn}, #{checkOut}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(AttendanceRecord record);

	@Update("update attendance_record set work_date = #{workDate}, check_in = #{checkIn}, check_out = #{checkOut}, status = #{status} where id = #{id}")
	int update(AttendanceRecord record);

	@Delete("delete from attendance_record where id = #{id}")
	int delete(Long id);
}
