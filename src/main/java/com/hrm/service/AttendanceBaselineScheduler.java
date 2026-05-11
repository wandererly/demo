package com.hrm.service;

import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.Employee;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.mapper.EmployeeMapper;
import java.time.LocalDate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceBaselineScheduler {

	private final EmployeeMapper employeeMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;

	public AttendanceBaselineScheduler(EmployeeMapper employeeMapper, AttendanceRecordMapper attendanceRecordMapper) {
		this.employeeMapper = employeeMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void generateTodayOnStartup() {
		generateForDate(LocalDate.now());
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void generateTodayBaseline() {
		generateForDate(LocalDate.now());
	}

	public void generateForDate(LocalDate workDate) {
		for (Employee employee : employeeMapper.findAll()) {
			if (employee == null || employee.getId() == null || !"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
				continue;
			}
			if (attendanceRecordMapper.findByEmpIdAndWorkDate(employee.getId(), workDate) != null) {
				continue;
			}
			AttendanceRecord record = new AttendanceRecord();
			record.setEmpId(employee.getId());
			record.setWorkDate(workDate);
			record.setStatus("ABSENT");
			attendanceRecordMapper.insert(record);
		}
	}
}
