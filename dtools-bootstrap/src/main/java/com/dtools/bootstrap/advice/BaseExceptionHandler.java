package com.dtools.bootstrap.advice;

import jakarta.servlet.http.HttpServletRequest;
import com.dtools.bootstrap.logging.LoggingRequestAttributes;
import org.slf4j.Logger;

/**
 * @description: 基础异常处理器，提供请求上下文和分级日志能力
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 仅放异常处理通用辅助方法，不承载具体异常映射策略
 */
public abstract class BaseExceptionHandler {

    /**
     * @description: 记录 info 级别异常日志
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 适用于客户端可见且风险较低的异常
     */
    protected void logInfo(Logger logger, Exception exception, HttpServletRequest request) {
        request.setAttribute(LoggingRequestAttributes.HANDLED_EXCEPTION, exception);
        logger.info("{}\n{}", exception.getMessage(), buildRequestContext(request), exception);
    }

    /**
     * @description: 记录 warn 级别异常日志
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 适用于业务失败、参数校验失败、权限异常等需要关注的失败
     */
    protected void logWarn(Logger logger, Exception exception, HttpServletRequest request) {
        request.setAttribute(LoggingRequestAttributes.HANDLED_EXCEPTION, exception);
        logger.warn("{}\n{}", exception.getMessage(), buildRequestContext(request), exception);
    }

    /**
     * @description: 记录 error 级别异常日志
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 适用于系统异常和未预期异常
     */
    protected void logError(Logger logger, Exception exception, HttpServletRequest request) {
        request.setAttribute(LoggingRequestAttributes.HANDLED_EXCEPTION, exception);
        logger.error("{}\n{}", exception.getMessage(), buildRequestContext(request), exception);
    }

    /**
     * @description: 构造请求上下文日志
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 不记录请求体，避免误写敏感数据
     */
    protected String buildRequestContext(HttpServletRequest request) {
        return "request: " + request.getRequestURI() + "\n"
                + "method: " + request.getMethod() + "\n"
                + "ua: " + request.getHeader("User-Agent") + "\n"
                + "ip: " + request.getRemoteAddr();
    }
}
