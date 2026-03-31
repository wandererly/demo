package com.hrm.service;

import com.hrm.domain.AttendanceRecord;
import com.hrm.dto.AttendanceCreateRequest;
import com.hrm.dto.AttendanceUpdateRequest;
import java.util.List;

public interface AttendanceService {
	List<AttendanceRecord> list();
	AttendanceRecord get(Long id);
	AttendanceRecord create(AttendanceCreateRequest request);
	AttendanceRecord update(Long id, AttendanceUpdateRequest request);
	void delete(Long id);
}
