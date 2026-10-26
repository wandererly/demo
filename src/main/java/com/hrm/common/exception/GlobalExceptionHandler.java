package com.hrm.common.exception;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
/** 将业务异常、权限异常、参数错误和数据库约束错误统一转换为 API 响应。 */
public class GlobalExceptionHandler {

	@ExceptionHandler(BizException.class)
	public ResponseEntity<ApiResponse<Void>> handleBiz(BizException ex) {
		return ResponseEntity.status(httpStatus(ex.getCode())).body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(ErrorCode.FORBIDDEN, "Forbidden"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getAllErrors().isEmpty()
				? "Validation error"
				: ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, message));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
		return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, "数据已存在或违反唯一性约束，请检查后重试"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
		return ResponseEntity.internalServerError().body(ApiResponse.fail(ErrorCode.SERVER_ERROR, "服务器处理失败"));
	}

	private HttpStatus httpStatus(int code) {
		if (code == ErrorCode.FORBIDDEN) return HttpStatus.FORBIDDEN;
		if (code == ErrorCode.NOT_FOUND) return HttpStatus.NOT_FOUND;
		if (code >= ErrorCode.SERVER_ERROR) return HttpStatus.INTERNAL_SERVER_ERROR;
		return HttpStatus.BAD_REQUEST;
	}
}
