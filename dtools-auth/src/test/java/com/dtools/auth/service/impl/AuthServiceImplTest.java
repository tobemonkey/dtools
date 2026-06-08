package com.dtools.auth.service.impl;

import com.dtools.auth.config.AuthProperties;
import com.dtools.auth.enums.AuthRole;
import com.dtools.auth.enums.UserStatus;
import com.dtools.auth.mapper.AuthMapper;
import com.dtools.auth.model.command.RefreshTokenCommand;
import com.dtools.auth.model.entity.AuthRefreshTokenEntity;
import com.dtools.auth.model.entity.AuthUserEntity;
import com.dtools.auth.token.JwtTokenService;
import com.dtools.auth.token.RefreshTokenService;
import com.dtools.common.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @description: 鉴权应用服务测试，覆盖 refresh token 单次消费关键行为
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前使用 Mock Mapper 验证服务层轮换顺序，避免依赖真实数据库
 */
class AuthServiceImplTest {

    /**
     * @description: 验证旧 refresh token 条件撤销失败时刷新失败且不插入新 token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: revoke 返回 0 代表旧 token 已被消费、撤销或过期，必须拒绝二次消费
     */
    @Test
    void refreshShouldFailWhenOldTokenCannotBeRevokedOnce() {
        AuthMapper authMapper = mock(AuthMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        AuthProperties authProperties = new AuthProperties();
        AuthServiceImpl authService = new AuthServiceImpl(
                authMapper,
                passwordEncoder,
                jwtTokenService,
                refreshTokenService,
                authProperties
        );

        RefreshTokenCommand command = new RefreshTokenCommand();
        command.setRefreshToken("old-refresh-token");
        AuthRefreshTokenEntity oldToken = new AuthRefreshTokenEntity();
        oldToken.setUserId(1L);
        AuthUserEntity user = new AuthUserEntity();
        user.setId(1L);
        user.setUsername("owner");
        user.setDisplayName("Owner");
        user.setStatus(UserStatus.ENABLED.getCode());

        when(refreshTokenService.hashToken("old-refresh-token")).thenReturn("old-hash");
        when(refreshTokenService.generateToken()).thenReturn("new-refresh-token");
        when(refreshTokenService.hashToken("new-refresh-token")).thenReturn("new-hash");
        when(authMapper.findActiveRefreshTokenByHash(eq("old-hash"), any(LocalDateTime.class))).thenReturn(oldToken);
        when(authMapper.findUserById(1L)).thenReturn(user);
        when(authMapper.findRoleCodesByUserId(1L)).thenReturn(List.of(AuthRole.OWNER.getCode()));
        when(authMapper.revokeRefreshToken(eq("old-hash"), any(LocalDateTime.class), any(LocalDateTime.class), eq("new-hash")))
                .thenReturn(0);

        assertThatThrownBy(() -> authService.refresh(command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("刷新凭证无效或已过期");

        verify(authMapper, never()).insertRefreshToken(any(AuthRefreshTokenEntity.class));
        verify(jwtTokenService, never()).issueAccessToken(any());
    }

    /**
     * @description: 验证旧 refresh token 缺失时刷新失败且不会生成新 token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该场景覆盖旧 token 二次消费后的再次刷新请求
     */
    @Test
    void refreshShouldFailWhenOldTokenAlreadyConsumed() {
        AuthMapper authMapper = mock(AuthMapper.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        AuthServiceImpl authService = new AuthServiceImpl(
                authMapper,
                mock(PasswordEncoder.class),
                mock(JwtTokenService.class),
                refreshTokenService,
                new AuthProperties()
        );
        when(refreshTokenService.hashToken(anyString())).thenReturn("old-hash");
        when(authMapper.findActiveRefreshTokenByHash(eq("old-hash"), any(LocalDateTime.class))).thenReturn(null);

        RefreshTokenCommand command = new RefreshTokenCommand();
        command.setRefreshToken("old-refresh-token");

        assertThatThrownBy(() -> authService.refresh(command))
                .isInstanceOf(AuthenticationException.class);
        verify(authMapper, never()).insertRefreshToken(any(AuthRefreshTokenEntity.class));
    }
}
