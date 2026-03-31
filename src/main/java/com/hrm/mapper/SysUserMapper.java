package com.hrm.mapper;

import com.hrm.domain.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper {

	@Select("select id, username, password_hash, role, status, created_at from sys_user order by id desc")
	List<SysUser> findAll();

	@Select("select id, username, password_hash, role, status, created_at from sys_user where id = #{id}")
	SysUser findById(Long id);

	@Select("select id, username, password_hash, role, status, created_at from sys_user where username = #{username}")
	SysUser findByUsername(String username);

	@Insert("insert into sys_user(username, password_hash, role, status, created_at) values(#{username}, #{passwordHash}, #{role}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SysUser user);

	@Update("update sys_user set username = #{username}, password_hash = #{passwordHash}, role = #{role}, status = #{status} where id = #{id}")
	int update(SysUser user);

	@Delete("delete from sys_user where id = #{id}")
	int delete(Long id);
}
