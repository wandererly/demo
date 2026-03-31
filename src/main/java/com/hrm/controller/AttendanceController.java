package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.AttendanceRecord;
import com.hrm.dto.AttendanceCreateRequest;
import com.hrm.dto.AttendanceUpdateRequest;
import com.hrm.service.AttendanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@Validated
@PreAuthorize("hasAuthority('attendance:manage')")
public class AttendanceController {

	private final AttendanceService attendanceService;

	public AttendanceController(AttendanceService attendanceService) {
		this.attendanceService = attendanceService;
	}

	@GetMapping
	public ApiResponse<List<AttendanceRecord>> list() {
		return ApiResponse.ok(attendanceService.list());
	}

	@GetMapping("/{id}")
	public ApiResponse<AttendanceRecord> get(@PathVariable Long id) {
		return ApiResponse.ok(attendanceService.get(id));
	}

	@PostMapping
	public ApiResponse<AttendanceRecord> create(@Valid @RequestBody AttendanceCreateRequest request) {
		return ApiResponse.ok(attendanceService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<AttendanceRecord> update(@PathVariable Long id, @RequestBody AttendanceUpdateRequest request) {
		return ApiResponse.ok(attendanceService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		attendanceService.delete(id);
		return ApiResponse.ok(null);
	}
}



