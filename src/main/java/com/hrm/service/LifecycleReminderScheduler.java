package com.hrm.service;

import com.hrm.domain.Employee;
import com.hrm.domain.EmployeeContract;
import com.hrm.domain.LifecycleEvent;
import com.hrm.domain.NotificationItem;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.EnterpriseMapper;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LifecycleReminderScheduler {

	private final EmployeeMapper employeeMapper;
	private final EnterpriseMapper enterpriseMapper;

	public LifecycleReminderScheduler(EmployeeMapper employeeMapper, EnterpriseMapper enterpriseMapper) {
		this.employeeMapper = employeeMapper;
		this.enterpriseMapper = enterpriseMapper;
	}

	@Scheduled(cron = "0 30 8 * * *")
	public void generateReminders() {
		LocalDate today = LocalDate.now();
		remindContracts(today);
		remindProbation(today);
		remindOverdueRegularization(today);
	}

	public void runOnceForStartup() {
		generateReminders();
	}

	private void remindContracts(LocalDate today) {
		for (EmployeeContract contract : enterpriseMapper.listContracts()) {
			if (contract.getEndDate() == null || contract.getEmpId() == null) {
				continue;
			}
			if (isWithin(today, contract.getEndDate(), 30)) {
				notifyOnce(contract.getEmpId(), "合同到期提醒",
						"合同 " + contract.getContractNo() + " 将于 " + contract.getEndDate() + " 到期",
						"CONTRACT_DUE", contract.getId());
			}
		}
	}

	private void remindProbation(LocalDate today) {
		for (Employee employee : employeeMapper.findAll()) {
			if (employee.getProbationEndDate() == null || "RESIGNED".equalsIgnoreCase(employee.getStatus())) {
				continue;
			}
			if (isWithin(today, employee.getProbationEndDate(), 7)) {
				notifyOnce(employee.getId(), "试用期到期提醒",
						employee.getName() + " 试用期将于 " + employee.getProbationEndDate() + " 到期",
						"PROBATION_DUE", employee.getId());
			}
		}
	}

	private void remindOverdueRegularization(LocalDate today) {
		for (LifecycleEvent event : enterpriseMapper.listLifecycleEvents()) {
			if (!"REGULARIZATION".equalsIgnoreCase(event.getEventType()) || !"PENDING".equalsIgnoreCase(event.getStatus())) {
				continue;
			}
			if (event.getEffectiveDate() != null && event.getEffectiveDate().isBefore(today)) {
				notifyOnce(event.getEmpId(), "转正待处理提醒",
						event.getTitle() + " 已超过生效日期，请尽快处理",
						"REGULARIZATION_OVERDUE", event.getId());
			}
		}
	}

	private boolean isWithin(LocalDate today, LocalDate target, int days) {
		return !target.isBefore(today) && !target.isAfter(today.plusDays(days));
	}

	private void notifyOnce(Long empId, String title, String content, String bizType, Long bizId) {
		if (enterpriseMapper.countUnreadNotificationByBiz(empId, bizType, bizId, title) > 0) {
			return;
		}
		NotificationItem notification = new NotificationItem();
		notification.setEmpId(empId);
		notification.setTitle(title);
		notification.setContent(content);
		notification.setBizType(bizType);
		notification.setBizId(bizId);
		notification.setStatus("UNREAD");
		enterpriseMapper.insertNotification(notification);
	}
}
