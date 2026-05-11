package com.hrm.service.impl;

import com.hrm.domain.AuditLog;
import com.hrm.mapper.AuditLogMapper;
import com.hrm.service.AuditLogService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

	private final AuditLogMapper auditLogMapper;

	public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
		this.auditLogMapper = auditLogMapper;
	}

	@Override
	public List<AuditLog> listRecent() {
		return auditLogMapper.findRecent();
	}

	@Override
	public void record(String module, String action, String targetType, Long targetId, String detail) {
		recordChange(module, action, targetType, targetId, null, null, detail);
	}

	@Override
	public void recordChange(String module, String action, String targetType, Long targetId, String beforeValue, String afterValue, String detail) {
		AuditLog log = new AuditLog();
		log.setModule(module);
		log.setAction(action);
		log.setTargetType(targetType);
		log.setTargetId(targetId);
		log.setActor(currentActor());
		log.setDetail(detail);
		log.setBeforeValue(beforeValue);
		log.setAfterValue(afterValue);
		auditLogMapper.insert(log);
	}

	private String currentActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			return "system";
		}
		return authentication.getName();
	}
}
