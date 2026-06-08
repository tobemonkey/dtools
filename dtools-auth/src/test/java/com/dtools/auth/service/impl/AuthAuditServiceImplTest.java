package com.dtools.auth.service.impl;

import com.dtools.auth.mapper.AuthMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @description: 鉴权审计服务测试，验证审计写入入口的容错行为
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 后续接入 MQ 或 Outbox 时应保留调用方不感知审计失败的约束
 */
class AuthAuditServiceImplTest {

    /**
     * @description: 验证登录审计写入异常不会向认证主流程传播
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 审计失败只应记录日志，不阻断登录失败或登录成功响应
     */
    @Test
    void recordLoginAuditShouldNotThrowWhenMapperFails() {
        AuthMapper authMapper = mock(AuthMapper.class);
        AuthAuditServiceImpl authAuditService = new AuthAuditServiceImpl(authMapper);
        when(authMapper.insertLoginAudit(eq(1L), eq("owner"), eq(true), eq(null), any(), any(LocalDateTime.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThatCode(() -> authAuditService.recordLoginAudit(1L, "owner", true, null))
                .doesNotThrowAnyException();

        verify(authMapper).insertLoginAudit(eq(1L), eq("owner"), eq(true), eq(null), any(), any(LocalDateTime.class));
    }
}
