package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationItem {
	private Long id;
	private String userName;
	private Long empId;
	private String title;
	private String content;
	private String status;
	private LocalDateTime createdAt;
}
