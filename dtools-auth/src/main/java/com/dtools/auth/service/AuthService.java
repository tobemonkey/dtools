package com.dtools.auth.service;

import com.dtools.auth.model.command.LoginCommand;
import com.dtools.auth.model.command.LogoutCommand;
import com.dtools.auth.model.command.RefreshTokenCommand;
import com.dtools.auth.model.dto.AuthTokenDTO;
import com.dtools.auth.model.dto.CurrentUserDTO;

/**
 * @description: 鉴权应用服务接口，向 Controller 暴露登录、刷新、退出和当前用户查询能力
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: Controller 不直接访问 Mapper 或 Token 细节，统一通过该服务完成用例
 */
public interface AuthService {

    /**
     * @description: 使用用户名和密码登录，签发 access token 与 refresh token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 账号不存在和密码错误统一返回认证失败，避免泄露账号存在性
     */
    AuthTokenDTO login(LoginCommand command);

    /**
     * @description: 使用 refresh token 换取新的登录凭证并轮换 refresh token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: refresh token 仅以 SHA-256 hash 参与查询和撤销
     */
    AuthTokenDTO refresh(RefreshTokenCommand command);

    /**
     * @description: 退出当前登录态并撤销客户端提交的 refresh token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 退出接口保持幂等，不向调用方暴露 token 是否存在
     */
    void logout(CurrentUserDTO currentUser, LogoutCommand command);

    /**
     * @description: 查询当前用户权限摘要
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该响应是前端路由、按钮和用户展示的可信输入
     */
    CurrentUserDTO me(Long userId);
}
