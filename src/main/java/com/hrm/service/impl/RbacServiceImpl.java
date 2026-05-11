package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.SysRole;
import com.hrm.domain.SysUser;
import com.hrm.domain.SysUserRole;
import com.hrm.dto.RoleCreateRequest;
import com.hrm.dto.RoleUpdateRequest;
import com.hrm.dto.UserCreateRequest;
import com.hrm.dto.UserRoleAssignRequest;
import com.hrm.dto.UserUpdateRequest;
import com.hrm.mapper.SysRoleMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.mapper.SysUserRoleMapper;
import com.hrm.service.RbacService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RbacServiceImpl implements RbacService {

	private final SysUserMapper sysUserMapper;
	private final SysRoleMapper sysRoleMapper;
	private final SysUserRoleMapper sysUserRoleMapper;
	private final PasswordEncoder passwordEncoder;

	public RbacServiceImpl(SysUserMapper sysUserMapper,
						  SysRoleMapper sysRoleMapper,
						  SysUserRoleMapper sysUserRoleMapper,
						  PasswordEncoder passwordEncoder) {
		this.sysUserMapper = sysUserMapper;
		this.sysRoleMapper = sysRoleMapper;
		this.sysUserRoleMapper = sysUserRoleMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<SysUser> listUsers() {
		return sysUserMapper.findAll();
	}

	@Override
	public SysUser createUser(UserCreateRequest request) {
		SysUser user = new SysUser();
		user.setUsername(request.getUsername());
		user.setPasswordHash(encodeIfNeeded(request.getPasswordHash()));
		user.setRole(request.getRole() == null ? "EMPLOYEE" : request.getRole());
		user.setEmpId(request.getEmpId());
		user.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
		sysUserMapper.insert(user);
		return user;
	}

	@Override
	public SysUser updateUser(Long id, UserUpdateRequest request) {
		SysUser user = sysUserMapper.findById(id);
		if (user == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "User not found");
		}
		user.setUsername(request.getUsername() == null ? user.getUsername() : request.getUsername());
		String passwordHash = request.getPasswordHash();
		user.setPasswordHash(passwordHash == null ? user.getPasswordHash() : encodeIfNeeded(passwordHash));
		user.setRole(request.getRole() == null ? user.getRole() : request.getRole());
		user.setEmpId(request.getEmpId() == null ? user.getEmpId() : request.getEmpId());
		user.setStatus(request.getStatus() == null ? user.getStatus() : request.getStatus());
		sysUserMapper.update(user);
		return user;
	}

	private String encodeIfNeeded(String rawOrHash) {
		if (rawOrHash == null) {
			return null;
		}
		if (rawOrHash.startsWith("$2a$") || rawOrHash.startsWith("$2b$") || rawOrHash.startsWith("$2y$")) {
			return rawOrHash;
		}
		return passwordEncoder.encode(rawOrHash);
	}

	@Override
	public void deleteUser(Long id) {
		int rows = sysUserMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "User not found");
		}
	}

	@Override
	public List<SysRole> listRoles() {
		return sysRoleMapper.findAll();
	}

	@Override
	public SysRole createRole(RoleCreateRequest request) {
		SysRole role = new SysRole();
		role.setRoleName(request.getRoleName());
		role.setRoleKey(request.getRoleKey());
		sysRoleMapper.insert(role);
		return role;
	}

	@Override
	public SysRole updateRole(Long id, RoleUpdateRequest request) {
		SysRole role = sysRoleMapper.findById(id);
		if (role == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Role not found");
		}
		role.setRoleName(request.getRoleName() == null ? role.getRoleName() : request.getRoleName());
		role.setRoleKey(request.getRoleKey() == null ? role.getRoleKey() : request.getRoleKey());
		sysRoleMapper.update(role);
		return role;
	}

	@Override
	public void deleteRole(Long id) {
		int rows = sysRoleMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Role not found");
		}
	}

	@Override
	public SysUserRole assignRole(UserRoleAssignRequest request) {
		SysUserRole userRole = new SysUserRole();
		userRole.setUserId(request.getUserId());
		userRole.setRoleId(request.getRoleId());
		sysUserRoleMapper.insert(userRole);
		return userRole;
	}
}
