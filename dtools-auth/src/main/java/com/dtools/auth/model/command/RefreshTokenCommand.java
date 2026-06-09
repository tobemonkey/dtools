package com.dtools.auth.model.command;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;
import jakarta.validation.constraints.NotBlank;

/**
 * @description: 刷新 Token 命令，使用 refresh token 换取新的登录凭证
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: refresh token 只允许服务端保存哈希，禁止明文落库
 */
public class RefreshTokenCommand {

    /**
     * 刷新凭证明文，仅用于本次哈希比对和轮换。
     */
    @LogSensitive(strategy = SensitiveStrategy.HIDDEN)
    @NotBlank(message = "刷新凭证不能为空")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
