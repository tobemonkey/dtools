package com.dtools.bootstrap.logging;

import com.dtools.common.trace.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @description: 请求详情日志过滤器，采集请求参数快照并投递异步日志写入器
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 日志采集失败不得影响请求主链路
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestDetailLogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDetailLogFilter.class);

    private final RequestDetailLogProperties properties;

    private final RequestBodySanitizer requestBodySanitizer;

    private final RequestDetailLogWriter requestDetailLogWriter;

    public RequestDetailLogFilter(RequestDetailLogProperties properties,
                                  RequestBodySanitizer requestBodySanitizer,
                                  RequestDetailLogWriter requestDetailLogWriter) {
        this.properties = properties;
        this.requestBodySanitizer = requestBodySanitizer;
        this.requestDetailLogWriter = requestDetailLogWriter;
    }

    /**
     * @description: 包装请求并在请求完成后记录详情日志
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 只在配置开启时启用，body 记录关闭时不额外缓存请求体
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        HttpServletRequest requestToUse = shouldWrapRequest(request) ? new ContentCachingRequestWrapper(request, properties.getMaxBodyLength()) : request;
        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            publishSafely(requestToUse, response, System.currentTimeMillis() - start);
        }
    }

    private boolean shouldWrapRequest(HttpServletRequest request) {
        return Boolean.TRUE.equals(properties.getIncludeBody())
                && isJsonRequest(request)
                && !(request instanceof ContentCachingRequestWrapper);
    }

    private void publishSafely(HttpServletRequest request, HttpServletResponse response, long costMs) {
        try {
            RequestDetailLogEvent event = new RequestDetailLogEvent();
            event.setTraceId(TraceContext.getTraceId());
            event.setUserId(currentUserId());
            event.setMethod(request.getMethod());
            event.setUri(request.getRequestURI());
            event.setQuery(Boolean.TRUE.equals(properties.getIncludeQuery()) ? request.getQueryString() : null);
            event.setStatus(response.getStatus());
            event.setCostMs(costMs);
            event.setIp(request.getRemoteAddr());
            event.setUserAgent(request.getHeader("User-Agent"));
            event.setBody(resolveBody(request));
            requestDetailLogWriter.write(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("请求详情日志采集失败, traceId={}, reason={}", TraceContext.getTraceId(), exception.getMessage());
        }
    }

    private String resolveBody(HttpServletRequest request) {
        if (!Boolean.TRUE.equals(properties.getIncludeBody()) || !isJsonRequest(request)) {
            return null;
        }
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        String body = new String(content, resolveCharset(request));
        return requestBodySanitizer.sanitize(body, requestBodyType(request), properties.getMaxBodyLength());
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

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    private Charset resolveCharset(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (RuntimeException exception) {
            return StandardCharsets.UTF_8;
        }
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return authentication.getName();
    }
}
