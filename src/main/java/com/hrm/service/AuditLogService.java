package com.hrm.service;

import com.hrm.domain.AuditLog;
import java.util.List;

public interface AuditLogService {
	List<AuditLog> listRecent();
	void record(String module, String action, String targetType, Long targetId, String detail);
	void recordChange(String module, String action, String targetType, Long targetId, String beforeValue, String afterValue, String detail);
}
