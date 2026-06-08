package com.dtools.bootstrap.config;

import com.dtools.common.enums.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: API 授权失败处理器，将缺少权限码场景转换为统一 403 JSON
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 已认证但权限不足时使用该处理器，不混用 401
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityResponseWriter securityResponseWriter;

    public ApiAccessDeniedHandler(SecurityResponseWriter securityResponseWriter) {
        this.securityResponseWriter = securityResponseWriter;
    }

    /**
     * @description: 处理授权失败响应
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 统一返回 FORBIDDEN，权限细节只进入服务端日志或审计
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        securityResponseWriter.write(response, HttpStatus.FORBIDDEN, ResponseCode.FORBIDDEN, ResponseCode.FORBIDDEN.getDesc());
    }
}
