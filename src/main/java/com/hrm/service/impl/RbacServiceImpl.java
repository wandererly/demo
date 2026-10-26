package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.SysRole;
import com.hrm.domain.SysUser;
import com.hrm.domain.SysUserRole;
import com.hrm.dto.RoleCreateRequest;
import com.hrm.dto.RoleUpdateRequest;
import com.hrm.dto.EmployeePasswordResetRequest;
import com.hrm.dto.PasswordResetRequest;
import com.hrm.dto.UserCreateRequest;
import com.hrm.dto.UserRoleAssignRequest;
import com.hrm.dto.UserUpdateRequest;
import com.hrm.mapper.SysRoleMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.mapper.SysUserRoleMapper;
import com.hrm.service.RbacService;
import com.hrm.service.EmployeeAccountService;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RbacServiceImpl implements RbacService {

	private final SysUserMapper sysUserMapper;
	private final SysRoleMapper sysRoleMapper;
	private final SysUserRoleMapper sysUserRoleMapper;
	private final PasswordEncoder passwordEncoder;
	private final EmployeeAccountService employeeAccountService;

	public RbacServiceImpl(SysUserMapper sysUserMapper,
						  SysRoleMapper sysRoleMapper,
						  SysUserRoleMapper sysUserRoleMapper,
						  PasswordEncoder passwordEncoder,
						  EmployeeAccountService employeeAccountService) {
		this.sysUserMapper = sysUserMapper;
		this.sysRoleMapper = sysRoleMapper;
		this.sysUserRoleMapper = sysUserRoleMapper;
		this.passwordEncoder = passwordEncoder;
		this.employeeAccountService = employeeAccountService;
	}

	@Override
	public List<SysUser> listUsers() {
		return sysUserMapper.findAll();
	}

	@Override
	public SysUser createUser(UserCreateRequest request) {
		SysUser user = new SysUser();
		user.setUsername(normalizeUsername(request.getUsername()));
		user.setPasswordHash(encodeIfNeeded(request.getPasswordHash()));
		user.setRole(normalizeRole(request.getRole(), "EMPLOYEE"));
		user.setEmpId(request.getEmpId());
		user.setStatus(normalizeStatus(request.getStatus(), "ACTIVE"));
		sysUserMapper.insert(user);
		return user;
	}

	@Override
	public SysUser updateUser(Long id, UserUpdateRequest request) {
		SysUser user = sysUserMapper.findById(id);
		if (user == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "User not found");
		}
		if (request.getUsername() != null) {
			user.setUsername(normalizeUsername(request.getUsername()));
		}
		String passwordHash = request.getPasswordHash();
		user.setPasswordHash(passwordHash == null || passwordHash.isBlank() ? user.getPasswordHash() : encodeIfNeeded(passwordHash));
		if (request.getRole() != null) {
			user.setRole(normalizeRole(request.getRole(), user.getRole()));
		}
		user.setEmpId(request.getEmpId() == null ? user.getEmpId() : request.getEmpId());
		if (request.getStatus() != null) {
			user.setStatus(normalizeStatus(request.getStatus(), user.getStatus()));
		}
		sysUserMapper.update(user);
		return user;
	}

	@Override
	public SysUser resetPassword(Long id, PasswordResetRequest request) {
		SysUser user = sysUserMapper.findById(id);
		if (user == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "User not found");
		}
		user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
		sysUserMapper.update(user);
		return user;
	}

	@Override
	public SysUser resetEmployeePassword(EmployeePasswordResetRequest request) {
		SysUser user = employeeAccountService.ensureEmployeeAccountByEmpNo(request.getEmpNo());
		if (user == null || user.getId() == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "员工账号不存在");
		}
		PasswordResetRequest passwordRequest = new PasswordResetRequest();
		passwordRequest.setPassword(request.getPassword());
		return resetPassword(user.getId(), passwordRequest);
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

	private String normalizeRequired(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BizException(ErrorCode.BAD_REQUEST, message);
		}
		return value.trim();
	}

	private String normalizeUsername(String username) {
		return normalizeRequired(username, "登录账号不能为空");
	}

	private String normalizeRole(String role, String defaultRole) {
		String value = role == null || role.isBlank() ? defaultRole : role.trim();
		String builtin = value.toUpperCase(Locale.ROOT);
		if (sysRoleMapper.findByKey(value) != null) {
			return value;
		}
		if (sysRoleMapper.findByKey(builtin) != null) {
			return builtin;
		}
		throw new BizException(ErrorCode.BAD_REQUEST, "角色编码不存在");
	}

	private String normalizeStatus(String status, String defaultStatus) {
		String value = status == null || status.isBlank() ? defaultStatus : status.trim().toUpperCase(Locale.ROOT);
		if ("INACTIVE".equals(value)) {
			value = "DISABLED";
		}
		if (!List.of("ACTIVE", "DISABLED").contains(value)) {
			throw new BizException(ErrorCode.BAD_REQUEST, "账号状态只能是启用或停用");
		}
		return value;
	}

	private String normalizeNewRoleKey(String roleKey) {
		return normalizeRequired(roleKey, "角色编码不能为空").toUpperCase(Locale.ROOT);
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
		role.setRoleName(normalizeRequired(request.getRoleName(), "角色名称不能为空"));
		role.setRoleKey(normalizeNewRoleKey(request.getRoleKey()));
		if (sysRoleMapper.findByKey(role.getRoleKey()) != null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "角色编码已存在");
		}
		sysRoleMapper.insert(role);
		return role;
	}

	@Override
	public SysRole updateRole(Long id, RoleUpdateRequest request) {
		SysRole role = sysRoleMapper.findById(id);
		if (role == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Role not found");
		}
		if (request.getRoleName() != null) {
			role.setRoleName(normalizeRequired(request.getRoleName(), "角色名称不能为空"));
		}
		if (request.getRoleKey() != null) {
			String roleKey = normalizeNewRoleKey(request.getRoleKey());
			SysRole existingKey = sysRoleMapper.findByKey(roleKey);
			if (existingKey != null && !existingKey.getId().equals(id)) {
				throw new BizException(ErrorCode.BAD_REQUEST, "角色编码已存在");
			}
			role.setRoleKey(roleKey);
		}
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
