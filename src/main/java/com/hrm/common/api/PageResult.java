package com.hrm.common.api;

import java.util.List;
import lombok.Data;

@Data
public class PageResult<T> {
	private List<T> records;
	private long total;
	private int page;
	private int size;
}
