package com.dtools.bootstrap.config;

import com.dtools.common.enums.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: API 认证失败处理器，将未登录或 Token 无效场景转换为统一 401 JSON
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 不返回 Spring Security 默认 HTML 或空响应
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityResponseWriter securityResponseWriter;

    public ApiAuthenticationEntryPoint(SecurityResponseWriter securityResponseWriter) {
        this.securityResponseWriter = securityResponseWriter;
    }

    /**
     * @description: 处理认证失败响应
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 统一返回 UNAUTHORIZED，避免暴露 Token 解析细节
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        securityResponseWriter.write(response, HttpStatus.UNAUTHORIZED, ResponseCode.UNAUTHORIZED, ResponseCode.UNAUTHORIZED.getDesc());
    }
}
