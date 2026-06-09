package com.dtools.bootstrap.controller;

import com.dtools.auth.model.command.LoginCommand;
import com.dtools.auth.model.command.LogoutCommand;
import com.dtools.auth.model.command.RefreshTokenCommand;
import com.dtools.auth.model.dto.AuthTokenDTO;
import com.dtools.auth.model.dto.CurrentUserDTO;
import com.dtools.auth.service.AuthService;
import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 鉴权接口控制器，暴露登录、刷新、退出和当前用户查询 API
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 所有接口保持 ApiResponse 响应协议，Token 校验由 Spring Security 过滤链负责
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @description: 用户名密码登录接口
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 登录失败统一返回 401，不暴露账号是否存在
     */
    @PostMapping("/login")
    public ApiResponse<AuthTokenDTO> login(@Valid @RequestBody LoginCommand command) {
        return ApiResponse.success(authService.login(command), TraceContext.getTraceId());
    }

    /**
     * @description: 刷新登录凭证接口
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: refresh token 每次成功刷新都会轮换，旧凭证立即撤销
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenDTO> refresh(@Valid @RequestBody RefreshTokenCommand command) {
        return ApiResponse.success(authService.refresh(command), TraceContext.getTraceId());
    }

    /**
     * @description: 退出登录接口，撤销当前客户端持有的 refresh token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: access token 自身不落库，退出仅撤销 refresh token
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody(required = false) LogoutCommand command) {
        authService.logout(currentUserFromJwt(jwt), command);
        return ApiResponse.success(null, TraceContext.getTraceId());
    }

    /**
     * @description: 查询当前用户身份、角色、权限和数据范围
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 前端权限体验只消费该接口返回，不自行推导角色权限
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(authService.me(userIdFromJwt(jwt)), TraceContext.getTraceId());
    }

    private CurrentUserDTO currentUserFromJwt(Jwt jwt) {
        CurrentUserDTO currentUser = new CurrentUserDTO();
        currentUser.setId(userIdFromJwt(jwt));
        return currentUser;
    }

    private Long userIdFromJwt(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
