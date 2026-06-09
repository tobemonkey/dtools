package com.dtools.bootstrap.config;

import com.dtools.common.trace.TraceContext;
import com.dtools.common.trace.TraceIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * @description: TraceID 请求过滤器，负责在每次 HTTP 请求入口建立链路追踪标识
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 请求结束必须清理线程上下文，避免线程复用串线
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * @description: 为请求设置 TraceID 并写回响应头
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 调用方传入 TraceID 时优先沿用，否则生成新的 TraceID
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceIdGenerator.nextTraceId();
        }
        TraceContext.setTraceId(traceId);
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
            TraceContext.clear();
        }
    }
}
