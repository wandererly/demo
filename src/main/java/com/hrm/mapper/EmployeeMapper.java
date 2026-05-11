package com.hrm.mapper;

import com.hrm.domain.Employee;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmployeeMapper {

	@Select("select id, emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at "
			+ "from employee order by id desc")
	List<Employee> findAll();

	@Select("select id, emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at "
			+ "from employee where id = #{id}")
	Employee findById(Long id);

	@Select("select id, emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at "
			+ "from employee where dept_id = #{deptId} order by id desc")
	List<Employee> findByDeptId(Long deptId);

	@Insert("insert into employee(emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at) "
			+ "values(#{empNo}, #{name}, #{gender}, #{phone}, #{email}, #{deptId}, #{position}, #{hireDate}, #{status}, #{baseSalary}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Employee employee);

	@Update("update employee set name = #{name}, gender = #{gender}, phone = #{phone}, email = #{email}, dept_id = #{deptId}, "
			+ "position = #{position}, hire_date = #{hireDate}, status = #{status}, base_salary = #{baseSalary}, updated_at = now() "
			+ "where id = #{id}")
	int update(Employee employee);

	@Delete("delete from employee where id = #{id}")
	int delete(Long id);
}
