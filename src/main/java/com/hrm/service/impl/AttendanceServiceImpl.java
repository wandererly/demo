package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.AttendanceRecord;
import com.hrm.dto.AttendanceCreateRequest;
import com.hrm.dto.AttendanceUpdateRequest;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.service.AuditLogService;
import com.hrm.service.AttendanceService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceServiceImpl implements AttendanceService {

	private final AttendanceRecordMapper attendanceRecordMapper;
	private final AuditLogService auditLogService;

	public AttendanceServiceImpl(AttendanceRecordMapper attendanceRecordMapper, AuditLogService auditLogService) {
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.auditLogService = auditLogService;
	}

	@Override
	public List<AttendanceRecord> list() {
		return attendanceRecordMapper.findAll();
	}

	@Override
	public AttendanceRecord get(Long id) {
		AttendanceRecord record = attendanceRecordMapper.findById(id);
		if (record == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Attendance record not found");
		}
		return record;
	}

	@Override
	public AttendanceRecord create(AttendanceCreateRequest request) {
		AttendanceRecord record = new AttendanceRecord();
		record.setEmpId(request.getEmpId());
		record.setWorkDate(request.getWorkDate());
		record.setCheckIn(request.getCheckIn());
		record.setCheckOut(request.getCheckOut());
		record.setStatus(request.getStatus() == null ? "NORMAL" : request.getStatus());
		attendanceRecordMapper.insert(record);
		auditLogService.record("考勤", "新增", "attendance_record", record.getId(), "员工ID " + record.getEmpId());
		return record;
	}

	public AttendanceRecord update(Long id, AttendanceUpdateRequest request) {
		AttendanceRecord existing = get(id);
		existing.setWorkDate(request.getWorkDate() == null ? existing.getWorkDate() : request.getWorkDate());
		existing.setCheckIn(request.getCheckIn() == null ? existing.getCheckIn() : request.getCheckIn());
		existing.setCheckOut(request.getCheckOut() == null ? existing.getCheckOut() : request.getCheckOut());
		existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
		attendanceRecordMapper.update(existing);
		auditLogService.record("考勤", "更新", "attendance_record", existing.getId(), "员工ID " + existing.getEmpId());
		return existing;
	}

	@Override
	public void delete(Long id) {
		int rows = attendanceRecordMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Attendance record not found");
		}
		auditLogService.record("考勤", "删除", "attendance_record", id, null);
	}
}
