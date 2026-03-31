package com.hrm.common.exception;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BizException.class)
	public ApiResponse<Void> handleBiz(BizException ex) {
		return ApiResponse.fail(ex.getCode(), ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ApiResponse<Void> handleDenied(AccessDeniedException ex) {
		return ApiResponse.fail(ErrorCode.FORBIDDEN, "Forbidden");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ApiResponse<Void> handleValid(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getAllErrors().isEmpty()
				? "Validation error"
				: ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		return ApiResponse.fail(ErrorCode.BAD_REQUEST, message);
	}

	@ExceptionHandler(Exception.class)
	public ApiResponse<Void> handleOther(Exception ex) {
		return ApiResponse.fail(ErrorCode.SERVER_ERROR, ex.getMessage());
	}
}
