package com.dtools.bootstrap.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @description: 接口 DB 请求日志过滤器，采集 API 请求耗时、状态和失败诊断信息
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 日志采集和写库失败不得影响请求主链路
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ApiRequestLogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestLogFilter.class);

    private final ApiRequestLogProperties properties;

    private final ApiRequestLogEventFactory apiRequestLogEventFactory;

    private final ApiRequestLogDbWriter apiRequestLogDbWriter;

    public ApiRequestLogFilter(ApiRequestLogProperties properties,
                               ApiRequestLogEventFactory apiRequestLogEventFactory,
                               ApiRequestLogDbWriter apiRequestLogDbWriter) {
        this.properties = properties;
        this.apiRequestLogEventFactory = apiRequestLogEventFactory;
        this.apiRequestLogDbWriter = apiRequestLogDbWriter;
    }

    /**
     * @description: 仅过滤 API 请求
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 非 API 请求不进入接口 DB 日志主账本
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    /**
     * @description: 包装请求响应并在请求完成后投递 DB 请求日志
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 异常会继续向外抛出，日志处理只在 finally 中做旁路采集
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = wrapRequest(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        Throwable thrownException = null;
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (ServletException | IOException | RuntimeException exception) {
            thrownException = exception;
            throw exception;
        } finally {
            publishSafely(requestWrapper, responseWrapper, thrownException, System.currentTimeMillis() - start);
            responseWrapper.copyBodyToResponse();
        }
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            return wrapper;
        }
        return new ContentCachingRequestWrapper(request, properties.getMaxRequestCacheLength());
    }

    private void publishSafely(ContentCachingRequestWrapper request,
                               ContentCachingResponseWrapper response,
                               Throwable thrownException,
                               long costMs) {
        try {
            request.setAttribute(LoggingRequestAttributes.REQUEST_BODY_TYPE, requestBodyType(request));
            Throwable handledException = handledException(request);
            Throwable exception = handledException != null ? handledException : thrownException;
            ApiRequestLogEvent event = apiRequestLogEventFactory.create(
                    request,
                    response,
                    responseBody(response),
                    exception,
                    costMs
            );
            apiRequestLogDbWriter.write(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("接口 DB 请求日志采集失败, reason={}", exception.getMessage());
        }
    }

    private Throwable handledException(HttpServletRequest request) {
        Object value = request.getAttribute(LoggingRequestAttributes.HANDLED_EXCEPTION);
        if (value instanceof Throwable throwable) {
            return throwable;
        }
        return null;
    }

    private Class<?> requestBodyType(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null;
        }
        for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                return parameter.getType();
            }
        }
        return null;
    }

    private String responseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        String contentType = response.getContentType();
        if (contentType != null && !contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return null;
        }
        return new String(content, resolveCharset(response.getCharacterEncoding()));
    }

    private Charset resolveCharset(String encoding) {
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (RuntimeException exception) {
            return StandardCharsets.UTF_8;
        }
    }
}

