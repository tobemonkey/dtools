package com.dtools.bootstrap.advice;

import com.dtools.common.enums.ResponseCode;
import com.dtools.common.exception.BizException;
import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description: 全局异常处理器，统一转换后端异常为 ApiResponse
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 不在 Controller 中分散拼装错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @description: 转换受控业务异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: message 面向调用方，应避免泄露内部实现细节
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception) {
        return ApiResponse.failure(exception.getResponseCode(), exception.getMessage(), TraceContext.getTraceId());
    }

    /**
     * @description: 转换未预期系统异常
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前返回统一系统异常文案，详细堆栈只进入日志
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.failure(ResponseCode.SYSTEM_ERROR, ResponseCode.SYSTEM_ERROR.getDesc(), TraceContext.getTraceId());
    }
}
