package com.dtools.bootstrap.advice;

import com.dtools.common.enums.ResponseCode;
import com.dtools.common.exception.AccessDeniedException;
import com.dtools.common.exception.ApplicationException;
import com.dtools.common.exception.AuthenticationException;
import com.dtools.common.exception.BizException;
import com.dtools.common.exception.ClientException;
import com.dtools.common.exception.SystemException;
import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * @description: 全局异常处理器，统一转换后端异常为 ApiResponse
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 不在 Controller 中分散拼装错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String APPLICATION_ERROR_MESSAGE = "业务处理异常，请稍后重试";

    private static final String SYSTEM_ERROR_MESSAGE = "系统异常，请稍后重试";

    /**
     * @description: 转换客户端可见异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该异常 message 会返回调用方，抛出时禁止放入内部敏感信息
     */
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleClientException(ClientException exception, HttpServletRequest request) {
        logInfo(LOGGER, exception, request);
        return failure(HttpStatus.BAD_REQUEST, exception.getResponseCode(), rawMessage(exception));
    }

    /**
     * @description: 转换应用业务异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 详细异常信息只进入日志，接口返回统一业务异常文案
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception, HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        return failure(HttpStatus.BAD_REQUEST, exception.getResponseCode(), APPLICATION_ERROR_MESSAGE);
    }

    /**
     * @description: 转换认证异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前仅预留认证语义，后续接入认证体系后复用
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception, HttpServletRequest request) {
        logInfo(LOGGER, exception, request);
        return failure(HttpStatus.UNAUTHORIZED, exception.getResponseCode(), rawMessage(exception));
    }

    /**
     * @description: 转换权限异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前仅预留权限语义，后续接入权限体系后复用
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        return failure(HttpStatus.FORBIDDEN, exception.getResponseCode(), rawMessage(exception));
    }

    /**
     * @description: 转换通用受控业务异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 兜底处理未细分的业务异常，返回异常自身 message
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException exception, HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        return failure(HttpStatus.BAD_REQUEST, exception.getResponseCode(), rawMessage(exception));
    }

    /**
     * @description: 转换请求体字段校验异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 返回所有字段错误摘要，便于前端定位表单问题
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return failure(HttpStatus.BAD_REQUEST, ResponseCode.PARAM_ERROR, message);
    }

    /**
     * @description: 转换路径参数或查询参数校验异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 用于处理 @Validated 参数约束失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return failure(HttpStatus.BAD_REQUEST, ResponseCode.PARAM_ERROR, message);
    }

    /**
     * @description: 转换参数类型不匹配异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 用于处理查询参数、路径参数类型转换失败
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        return failure(HttpStatus.BAD_REQUEST, ResponseCode.PARAM_ERROR, "参数类型错误: " + exception.getName());
    }

    /**
     * @description: 转换 JSON 请求体解析异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 不返回底层解析细节，避免暴露内部结构
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        logWarn(LOGGER, exception, request);
        return failure(HttpStatus.BAD_REQUEST, ResponseCode.PARAM_ERROR, "请求体格式错误");
    }

    /**
     * @description: 转换系统异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 详细异常信息只进入日志，接口返回统一系统异常文案
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException exception, HttpServletRequest request) {
        logError(LOGGER, exception, request);
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, exception.getResponseCode(), SYSTEM_ERROR_MESSAGE);
    }

    /**
     * @description: 转换未预期系统异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前返回统一系统异常文案，详细堆栈只进入日志
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception, HttpServletRequest request) {
        logError(LOGGER, exception, request);
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.SYSTEM_ERROR, SYSTEM_ERROR_MESSAGE);
    }

    private ResponseEntity<ApiResponse<Void>> failure(HttpStatus httpStatus, ResponseCode responseCode, String message) {
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.failure(responseCode, message, TraceContext.getTraceId()));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String rawMessage(Exception exception) {
        if (exception instanceof BizException bizException) {
            return bizException.getRawMessage();
        }
        return exception.getMessage();
    }
}
