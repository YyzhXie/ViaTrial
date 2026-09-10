package com.viatrial.exception;

import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 *
 * <p>审计项 A-2：此前所有 handler 都不记录日志，未映射异常统一返回 500 后在运行时完全不可见。
 * 现在每个分支都留下日志：客户端错误记 {@code warn}，服务端错误记 {@code error} 并带异常栈，
 * 便于事后定位。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException ex, HttpServletRequest request) {
        HttpStatus status = resolveStatus(ex.getCode());
        if (status.is5xxServerError()) {
            log.error("Business exception while handling {} {}: code={}, message={}",
                    request.getMethod(), request.getRequestURI(), ex.getCode(), ex.getMessage(), ex);
        } else {
            log.warn("Rejected {} {}: code={}, message={}",
                    request.getMethod(), request.getRequestURI(), ex.getCode(), ex.getMessage());
        }
        return ResponseEntity.status(status).body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                             HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("Invalid request body for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return badRequest(message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("Invalid request parameters for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return badRequest(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                          HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return badRequest(message);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Result<Void>> handleRequestParameterException(Exception ex, HttpServletRequest request) {
        log.warn("Malformed request parameters for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return badRequest(ex.getMessage());
    }

    /**
     * 请求体无法解析（例如 JSON 语法错误、difficulty 传了非数字）。此前会落到 500，
     * 语义上属于客户端错误。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                         HttpServletRequest request) {
        log.warn("Unreadable request body for {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return badRequest(ErrorCode.PARAM_ERROR.getMessage());
    }

    /**
     * 数据库约束冲突（UNIQUE / NOT NULL / FOREIGN KEY / CHECK）。此前会落到 500，
     * 现在返回 409 并记录日志，既保留数据完整性语义又便于排查。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                                             HttpServletRequest request) {
        log.error("Data integrity violation while handling {} {}",
                request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.fail(ErrorCode.CONFLICT, "数据约束冲突，操作已回滚"));
    }

    /**
     * 静态资源不存在（例如已关闭的 {@code /v3/api-docs}、写错的路径）。
     * 此前会落到兜底 500 并打错误栈，语义上应返回 404 且只记 warn。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException ex,
                                                                      HttpServletRequest request) {
        log.warn("Resource not found for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.fail(ErrorCode.NOT_FOUND, "资源不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception while handling {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.SYSTEM_ERROR));
    }

    private ResponseEntity<Result<Void>> badRequest(String message) {
        String finalMessage = message == null || message.isBlank() ? ErrorCode.PARAM_ERROR.getMessage() : message;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(ErrorCode.PARAM_ERROR, finalMessage));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    private HttpStatus resolveStatus(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
    }
}
