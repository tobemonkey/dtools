package com.dtools.auth.model.command;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;

/**
 * @description: 退出登录命令，用于撤销客户端当前持有的 refresh token
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: refreshToken 为空时只完成幂等退出，不暴露服务端 Token 状态
 */
public class LogoutCommand {

    /**
     * 待撤销的刷新凭证明文。
     */
    @LogSensitive(strategy = SensitiveStrategy.TOKEN)
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
