package com.hrm.mapper;

import com.hrm.domain.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface SysUserRoleMapper {

	@Insert("insert into sys_user_role(user_id, role_id) values(#{userId}, #{roleId})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SysUserRole userRole);

	@Delete("delete from sys_user_role where user_id = #{userId} and role_id = #{roleId}")
	int delete(SysUserRole userRole);
}
