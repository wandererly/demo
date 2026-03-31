package com.hrm.auth;

import com.hrm.domain.Permission;
import com.hrm.domain.RolePermission;
import com.hrm.domain.SysRole;
import com.hrm.mapper.PermissionMapper;
import com.hrm.mapper.RolePermissionMapper;
import com.hrm.mapper.SysRoleMapper;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PermissionInitializer implements CommandLineRunner {

	private final PermissionMapper permissionMapper;
	private final SysRoleMapper sysRoleMapper;
	private final RolePermissionMapper rolePermissionMapper;

	public PermissionInitializer(PermissionMapper permissionMapper,
							  SysRoleMapper sysRoleMapper,
							  RolePermissionMapper rolePermissionMapper) {
		this.permissionMapper = permissionMapper;
		this.sysRoleMapper = sysRoleMapper;
		this.rolePermissionMapper = rolePermissionMapper;
	}

	@Override
	public void run(String... args) {
		// Startup check: remove duplicate role-permission rows
		rolePermissionMapper.cleanDuplicates();

		SysRole admin = ensureRole("ADMIN", "系统管理员");
		SysRole hr = ensureRole("HR", "人事专员");

		String[][] perms = new String[][] {
				{"rbac:manage", "权限管理"},
				{"config:manage", "系统配置"},
				{"hr:manage", "员工与部门管理"},
				{"attendance:manage", "考勤管理"},
				{"leave:manage", "请假管理"},
				{"perf:manage", "绩效管理"},
				{"payroll:manage", "薪酬管理"}
		};

		for (String[] p : perms) {
			Permission permission = ensurePermission(p[0], p[1]);
			assignIfMissing(admin, permission);
		}

		for (String[] p : perms) {
			if ("rbac:manage".equals(p[0])) {
				continue;
			}
			Permission permission = ensurePermission(p[0], p[1]);
			assignIfMissing(hr, permission);
		}
	}

	private SysRole ensureRole(String key, String name) {
		SysRole existing = sysRoleMapper.findByKey(key);
		if (existing != null) {
			return existing;
		}
		SysRole role = new SysRole();
		role.setRoleKey(key);
		role.setRoleName(name);
		sysRoleMapper.insert(role);
		return role;
	}

	private Permission ensurePermission(String key, String name) {
		Permission existing = permissionMapper.findByKey(key);
		if (existing != null) {
			return existing;
		}
		Permission permission = new Permission();
		permission.setPermKey(key);
		permission.setPermName(name);
		permissionMapper.insert(permission);
		return permission;
	}

	private void assignIfMissing(SysRole role, Permission permission) {
		List<String> existing = rolePermissionMapper.findPermKeysByRoleKey(role.getRoleKey());
		if (existing != null && existing.contains(permission.getPermKey())) {
			return;
		}
		RolePermission rp = new RolePermission();
		rp.setRoleId(role.getId());
		rp.setPermId(permission.getId());
		rolePermissionMapper.insert(rp);
	}
}

