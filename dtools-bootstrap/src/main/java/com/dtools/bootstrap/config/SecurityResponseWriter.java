package com.dtools.bootstrap.config;

import com.dtools.common.enums.ResponseCode;
import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description: Spring Security 异常响应写入器，统一输出 ApiResponse JSON
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: Security Filter 阶段不会进入 ControllerAdvice，必须在这里显式写 JSON
 */
@Component
public class SecurityResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @description: 写出认证或授权失败响应
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 响应码必须来自统一 ResponseCode 枚举
     */
    public void write(HttpServletResponse response,
                      HttpStatus httpStatus,
                      ResponseCode responseCode,
                      String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(responseCode, message, TraceContext.getTraceId());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
