package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.Permission;
import com.hrm.dto.PermissionCreateRequest;
import com.hrm.dto.RolePermAssignRequest;
import com.hrm.service.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
@Validated
@PreAuthorize("hasAuthority('rbac:manage')")
public class PermissionController {

	private final PermissionService permissionService;

	public PermissionController(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@GetMapping
	public ApiResponse<List<Permission>> list() {
		return ApiResponse.ok(permissionService.listPermissions());
	}

	@PostMapping
	public ApiResponse<Permission> create(@Valid @RequestBody PermissionCreateRequest request) {
		return ApiResponse.ok(permissionService.createPermission(request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		permissionService.deletePermission(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/role/{roleKey}")
	public ApiResponse<List<String>> listByRole(@PathVariable String roleKey) {
		return ApiResponse.ok(permissionService.listPermKeysByRole(roleKey));
	}

	@PostMapping("/assign")
	public ApiResponse<Void> assign(@Valid @RequestBody RolePermAssignRequest request) {
		permissionService.assignPerm(request);
		return ApiResponse.ok(null);
	}

	@PostMapping("/revoke")
	public ApiResponse<Void> revoke(@Valid @RequestBody RolePermAssignRequest request) {
		permissionService.revokePerm(request);
		return ApiResponse.ok(null);
	}
}
