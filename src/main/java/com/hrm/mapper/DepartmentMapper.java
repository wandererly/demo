package com.hrm.mapper;

import com.hrm.domain.Department;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DepartmentMapper {

	@Select("select id, name, parent_id, manager_id, status, created_at, updated_at from department order by id desc")
	List<Department> findAll();

	@Select("select id, name, parent_id, manager_id, status, created_at, updated_at from department where id = #{id}")
	Department findById(Long id);

	@Insert("insert into department(name, parent_id, manager_id, status, created_at, updated_at) "
			+ "values(#{name}, #{parentId}, #{managerId}, #{status}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Department department);

	@Update("update department set name = #{name}, parent_id = #{parentId}, manager_id = #{managerId}, "
			+ "status = #{status}, updated_at = now() where id = #{id}")
	int update(Department department);

	@Delete("delete from department where id = #{id}")
	int delete(Long id);
}
