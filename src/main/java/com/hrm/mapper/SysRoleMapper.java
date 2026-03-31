package com.hrm.mapper;

import com.hrm.domain.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysRoleMapper {

	@Select("select id, role_name, role_key from sys_role order by id desc")
	List<SysRole> findAll();

	@Select("select id, role_name, role_key from sys_role where id = #{id}")
	SysRole findById(Long id);

	@Select("select id, role_name, role_key from sys_role where role_key = #{roleKey}")
	SysRole findByKey(String roleKey);

	@Insert("insert into sys_role(role_name, role_key) values(#{roleName}, #{roleKey})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SysRole role);

	@Update("update sys_role set role_name = #{roleName}, role_key = #{roleKey} where id = #{id}")
	int update(SysRole role);

	@Delete("delete from sys_role where id = #{id}")
	int delete(Long id);
}
