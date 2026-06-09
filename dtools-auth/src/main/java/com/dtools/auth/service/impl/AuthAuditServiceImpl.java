package com.dtools.auth.service.impl;

import com.dtools.auth.mapper.AuthMapper;
import com.dtools.auth.service.AuthAuditService;
import com.dtools.common.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @description: 鉴权审计服务实现，当前使用独立事务写入登录审计
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 该类是后续接入 MQ 或 Outbox 的统一替换入口
 */
@Service
public class AuthAuditServiceImpl implements AuthAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAuditServiceImpl.class);

    private final AuthMapper authMapper;

    public AuthAuditServiceImpl(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    /**
     * @description: 使用独立事务记录登录审计事件
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 审计写入失败只记录日志，不回滚或阻断认证主事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginAudit(Long userId, String username, Boolean success, String failureReason) {
        try {
            authMapper.insertLoginAudit(userId, username, success, failureReason, TraceContext.getTraceId(), LocalDateTime.now());
        } catch (RuntimeException exception) {
            LOGGER.warn("记录登录审计失败, userId={}, username={}, success={}, traceId={}, reason={}",
                    userId, username, success, TraceContext.getTraceId(), exception.getMessage());
        }
    }
}
