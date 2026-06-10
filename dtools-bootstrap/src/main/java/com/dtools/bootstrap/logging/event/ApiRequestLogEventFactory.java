package com.dtools.bootstrap.logging.event;

import com.dtools.bootstrap.logging.context.LoggingRequestAttributes;
import com.dtools.bootstrap.logging.sanitize.RequestErrorParamSnapshotBuilder;
import com.dtools.common.exception.SystemException;
import com.dtools.common.trace.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @description: 接口 DB 请求日志事件工厂，负责从请求、响应和异常上下文提取日志字段
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: responseCode 是业务协议码，失败判定需结合 HTTP 状态码
 */
@Component
public class ApiRequestLogEventFactory {

    private static final int DEFAULT_MESSAGE_MAX_LENGTH = 2048;

    private static final int DEFAULT_STACK_MAX_LENGTH = 20000;

    private static final int DEFAULT_PARAMS_MAX_LENGTH = 20000;

    private static final String API_RESPONSE_FAILURE = "API_RESPONSE_FAILURE";

    private static final String HTTP_STATUS_FAILURE = "HTTP_STATUS_FAILURE";

    private final ObjectMapper objectMapper;

    private final RequestErrorParamSnapshotBuilder requestErrorParamSnapshotBuilder;

    public ApiRequestLogEventFactory(ObjectMapper objectMapper,
                                     RequestErrorParamSnapshotBuilder requestErrorParamSnapshotBuilder) {
        this.objectMapper = objectMapper;
        this.requestErrorParamSnapshotBuilder = requestErrorParamSnapshotBuilder;
    }

    /**
     * @description: 创建接口 DB 请求日志事件
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 成功请求不记录异常栈和失败参数快照
     */
    public ApiRequestLogEvent create(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String responseBody,
                                     Throwable exception,
                                     Long costMs) {
        ApiRequestLogEvent event = new ApiRequestLogEvent();
        event.setTraceId(TraceContext.getTraceId());
        event.setMethod(request.getMethod());
        event.setUri(request.getRequestURI());
        event.setClientIp(request.getRemoteAddr());
        event.setUserAgent(limit(request.getHeader("User-Agent"), 512));
        event.setHttpStatus(response.getStatus());
        event.setCostMs(costMs);
        applyResponseBody(event, responseBody);
        applyFailureFields(event, request, exception);
        return event;
    }

    private void applyResponseBody(ApiRequestLogEvent event, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode codeNode = root.get("code");
            if (codeNode != null && codeNode.isInt()) {
                event.setResponseCode(codeNode.asInt());
            }
            JsonNode messageNode = root.get("message");
            if (messageNode != null && messageNode.isTextual() && event.isFailure()) {
                event.setExceptionMessage(limit(messageNode.asText(), DEFAULT_MESSAGE_MAX_LENGTH));
            }
        } catch (Exception ignored) {
            // 非 ApiResponse 响应不影响请求日志主流程。
        }
    }

    private void applyFailureFields(ApiRequestLogEvent event, HttpServletRequest request, Throwable exception) {
        if (!event.isFailure()) {
            return;
        }
        if (exception != null) {
            event.setExceptionType(exception.getClass().getName());
            event.setExceptionMessage(limit(exception.getMessage(), DEFAULT_MESSAGE_MAX_LENGTH));
            if (shouldRecordStack(event, exception)) {
                event.setExceptionStack(limit(stackTrace(exception), DEFAULT_STACK_MAX_LENGTH));
            }
        } else {
            event.setExceptionType(event.getResponseCode() != null ? API_RESPONSE_FAILURE : HTTP_STATUS_FAILURE);
        }
        event.setRequestParamsOnError(requestErrorParamSnapshotBuilder.build(
                request.getQueryString(),
                requestBody(request),
                requestBodyType(request),
                DEFAULT_PARAMS_MAX_LENGTH
        ));
    }

    private boolean shouldRecordStack(ApiRequestLogEvent event, Throwable exception) {
        if (exception instanceof SystemException) {
            return true;
        }
        if (exception instanceof HttpMessageNotReadableException
                || exception instanceof MethodArgumentNotValidException
                || exception instanceof MethodArgumentTypeMismatchException
                || exception instanceof BindException) {
            return true;
        }
        return event.getHttpStatus() != null && event.getHttpStatus() >= 500;
    }

    private String requestBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        return new String(content, resolveCharset(request));
    }

    private Class<?> requestBodyType(HttpServletRequest request) {
        Object type = request.getAttribute(LoggingRequestAttributes.REQUEST_BODY_TYPE);
        if (type instanceof Class<?> bodyType) {
            return bodyType;
        }
        Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (bestMatchingPattern instanceof org.springframework.web.method.HandlerMethod handlerMethod) {
            for (java.lang.reflect.Parameter parameter : handlerMethod.getMethod().getParameters()) {
                if (parameter.isAnnotationPresent(org.springframework.web.bind.annotation.RequestBody.class)) {
                    return parameter.getType();
                }
            }
        }
        return null;
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

    private String stackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private String limit(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
