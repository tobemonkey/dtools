package com.dtools.auth.service;

/**
 * @description: 鉴权审计服务接口，统一承接登录审计事件写入
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 后续切换 MQ、Outbox 或异步投递时，只替换该服务实现，不修改登录流程各调用点
 */
public interface AuthAuditService {

    /**
     * @description: 记录登录审计事件
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 审计失败不得影响登录主流程，调用方不应感知底层写入失败
     */
    void recordLoginAudit(Long userId, String username, Boolean success, String failureReason);
}
