package com.dtools.auth.model.command;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;
import jakarta.validation.constraints.NotBlank;

/**
 * @description: 登录命令，承载账号密码登录所需参数
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: password 只用于本次校验，禁止写入日志或审计明细
 */
public class LoginCommand {

    /**
     * 登录用户名。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 登录密码明文，仅在请求内短暂存在。
     */
    @LogSensitive(strategy = SensitiveStrategy.MASK)
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
