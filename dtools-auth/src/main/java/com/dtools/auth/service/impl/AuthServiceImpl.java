package com.dtools.auth.service.impl;

import com.dtools.auth.config.AuthProperties;
import com.dtools.auth.enums.UserStatus;
import com.dtools.auth.mapper.AuthMapper;
import com.dtools.auth.model.command.LoginCommand;
import com.dtools.auth.model.command.LogoutCommand;
import com.dtools.auth.model.command.RefreshTokenCommand;
import com.dtools.auth.model.dto.AuthTokenDTO;
import com.dtools.auth.model.dto.CurrentUserDTO;
import com.dtools.auth.model.entity.AuthRefreshTokenEntity;
import com.dtools.auth.model.entity.AuthUserEntity;
import com.dtools.auth.security.AuthUserAssembler;
import com.dtools.auth.service.AuthAuditService;
import com.dtools.auth.service.AuthService;
import com.dtools.auth.token.JwtTokenService;
import com.dtools.auth.token.RefreshTokenService;
import com.dtools.common.exception.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 鉴权应用服务实现，负责账号登录、Token 刷新轮换、退出撤销和当前用户查询
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 密码校验统一使用 PasswordEncoder，refresh token 只保存 SHA-256 hash
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private static final String LOGIN_FAILED_MESSAGE = "用户名或密码错误";

    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    private final RefreshTokenService refreshTokenService;

    private final AuthProperties authProperties;

    private final AuthAuditService authAuditService;

    public AuthServiceImpl(AuthMapper authMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           RefreshTokenService refreshTokenService,
                           AuthProperties authProperties,
                           AuthAuditService authAuditService) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.authProperties = authProperties;
        this.authAuditService = authAuditService;
    }

    /**
     * @description: 使用用户名和密码登录，校验账号状态后签发访问令牌和刷新凭证
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 账号不存在和密码错误统一返回认证失败，避免泄露账号存在性
     */
    @Override
    @Transactional
    public AuthTokenDTO login(LoginCommand command) {
        AuthUserEntity user = authMapper.findUserByUsername(command.getUsername());
        if (user == null || !passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
            authAuditService.recordLoginAudit(null, command.getUsername(), false, LOGIN_FAILED_MESSAGE);
            throw new AuthenticationException(LOGIN_FAILED_MESSAGE);
        }
        if (!UserStatus.ENABLED.getCode().equals(user.getStatus())) {
            authAuditService.recordLoginAudit(user.getId(), user.getUsername(), false, "账号已禁用");
            throw new AuthenticationException("账号已禁用");
        }
        CurrentUserDTO currentUser = buildCurrentUser(user);
        AuthTokenDTO token = issueTokenPair(currentUser);
        authAuditService.recordLoginAudit(user.getId(), user.getUsername(), true, null);
        return token;
    }

    /**
     * @description: 使用 refresh token 刷新登录凭证，并通过条件撤销确保旧凭证单次消费
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 必须先成功撤销旧 hash 后再插入新 hash，撤销影响行数不是 1 时视为认证失败
     */
    @Override
    @Transactional
    public AuthTokenDTO refresh(RefreshTokenCommand command) {
        String oldHash = refreshTokenService.hashToken(command.getRefreshToken());
        LocalDateTime now = LocalDateTime.now();
        AuthRefreshTokenEntity oldToken = authMapper.findActiveRefreshTokenByHash(oldHash, now);
        if (oldToken == null) {
            throw new AuthenticationException("刷新凭证无效或已过期");
        }
        AuthUserEntity user = authMapper.findUserById(oldToken.getUserId());
        if (user == null || !UserStatus.ENABLED.getCode().equals(user.getStatus())) {
            authMapper.revokeRefreshToken(oldHash, now, now, null);
            throw new AuthenticationException("刷新凭证无效或已过期");
        }
        CurrentUserDTO currentUser = buildCurrentUser(user);
        String refreshToken = refreshTokenService.generateToken();
        String refreshTokenHash = refreshTokenService.hashToken(refreshToken);
        int revokedRows = authMapper.revokeRefreshToken(oldHash, now, now, refreshTokenHash);
        if (revokedRows != 1) {
            throw new AuthenticationException("刷新凭证无效或已过期");
        }
        insertRefreshToken(currentUser.getId(), refreshTokenHash, now);
        return buildTokenResponse(currentUser, refreshToken);
    }

    /**
     * @description: 退出当前登录态并撤销客户端提交的 refresh token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 退出保持幂等，撤销失败不向调用方暴露 token 状态
     */
    @Override
    @Transactional
    public void logout(CurrentUserDTO currentUser, LogoutCommand command) {
        if (command == null || command.getRefreshToken() == null || command.getRefreshToken().isBlank()) {
            return;
        }
        String tokenHash = refreshTokenService.hashToken(command.getRefreshToken());
        LocalDateTime now = LocalDateTime.now();
        authMapper.revokeRefreshToken(tokenHash, now, now, null);
    }

    /**
     * @description: 根据 JWT subject 重新查询当前用户状态、角色、权限和数据范围
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 不直接信任旧 JWT claims 中的展示名、角色或权限，避免用户状态变化后 me 返回陈旧数据
     */
    @Override
    public CurrentUserDTO me(Long userId) {
        AuthUserEntity user = authMapper.findUserById(userId);
        if (user == null || !UserStatus.ENABLED.getCode().equals(user.getStatus())) {
            throw new AuthenticationException("认证用户不存在或已禁用");
        }
        return buildCurrentUser(user);
    }

    private AuthTokenDTO issueTokenPair(CurrentUserDTO currentUser) {
        String refreshToken = refreshTokenService.generateToken();
        String refreshTokenHash = refreshTokenService.hashToken(refreshToken);
        LocalDateTime now = LocalDateTime.now();
        insertRefreshToken(currentUser.getId(), refreshTokenHash, now);
        return buildTokenResponse(currentUser, refreshToken);
    }

    private void insertRefreshToken(Long userId, String refreshTokenHash, LocalDateTime now) {
        AuthRefreshTokenEntity entity = new AuthRefreshTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(refreshTokenHash);
        entity.setExpiresAt(now.plusSeconds(authProperties.getRefreshTokenSeconds()));
        entity.setCreatedAt(now);
        authMapper.insertRefreshToken(entity);
    }

    private AuthTokenDTO buildTokenResponse(CurrentUserDTO currentUser, String refreshToken) {
        AuthTokenDTO token = new AuthTokenDTO();
        token.setAccessToken(jwtTokenService.issueAccessToken(currentUser));
        token.setRefreshToken(refreshToken);
        token.setTokenType(TOKEN_TYPE);
        token.setExpiresInSeconds(jwtTokenService.accessTokenSeconds());
        token.setCurrentUser(currentUser);
        return token;
    }

    private CurrentUserDTO buildCurrentUser(AuthUserEntity user) {
        List<String> roleCodes = authMapper.findRoleCodesByUserId(user.getId());
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new AuthenticationException("账号未分配角色");
        }
        return AuthUserAssembler.assemble(user, roleCodes);
    }

}
